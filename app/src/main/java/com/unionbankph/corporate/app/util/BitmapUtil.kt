package com.unionbankph.corporate.app.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore.Images.ImageColumns
import timber.log.Timber
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Provides static functions to decode bitmaps at the optimal size
 */
class BitmapUtil(
    private val context: Context
) {

    private val TAG = "BitmapUtils"
    private val DEFAULT_COMPRESS_QUALITY = 90
    private val MAX_COMPRESS_QUALITY = 100
    private val INDEX_ORIENTATION = 0
    private val DEBUG = false

    private val IMAGE_PROJECTION = arrayOf(ImageColumns.ORIENTATION)

    /**
     * Decode an image into a Bitmap, using sub-sampling if the hinted dimensions call for it.
     * Does not crop to fit the hinted dimensions.
     *
     * @param src an encoded image
     * @param w hint width in px
     * @param h hint height in px
     * @return a decoded Bitmap that is not exactly sized to the hinted dimensions.
     */
    fun decodeByteArray(src: ByteArray, w: Int, h: Int): Bitmap? {
        try {
            // calculate sample size based on w/h
            val opts = BitmapFactory.Options()
            opts.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(src, 0, src.size, opts)
            if (opts.mCancel || opts.outWidth == -1 || opts.outHeight == -1) {
                return null
            }
            opts.inSampleSize = Math.min(opts.outWidth / w, opts.outHeight / h)
            opts.inJustDecodeBounds = false
            return BitmapFactory.decodeByteArray(src, 0, src.size, opts)
        } catch (t: Throwable) {
            Timber.d("unable to decode image")
            return null
        }
    }

    /**
     * Decode an image into a Bitmap, using sub-sampling if the desired dimensions call for it.
     * Also applies a center-crop a la [android.widget.ImageView.ScaleType.CENTER_CROP].
     *
     * @param src an encoded image
     * @param w desired width in px
     * @param h desired height in px
     * @return an exactly-sized decoded Bitmap that is center-cropped.
     */
    fun decodeByteArrayWithCenterCrop(src: ByteArray, w: Int, h: Int): Bitmap? {
        try {
            val decoded = decodeByteArray(src, w, h)
            return centerCrop(decoded, w, h)
        } catch (t: Throwable) {
            Timber.d("unable to crop image")
            return null
        }
    }

    /**
     * Returns a new Bitmap copy with a center-crop effect a la
     * [android.widget.ImageView.ScaleType.CENTER_CROP]. May return the input bitmap if no
     * scaling is necessary.
     *
     * @param src original bitmap of any size
     * @param w desired width in px
     * @param h desired height in px
     * @return a copy of src conforming to the given width and height, or src itself if it already
     * matches the given width and height
     */
    fun centerCrop(src: Bitmap?, w: Int, h: Int): Bitmap {
        return crop(src, w, h, 0.5f, 0.5f)
    }

    /**
     * Returns a new Bitmap copy with a crop effect depending on the crop anchor given. 0.5f is like
     * [android.widget.ImageView.ScaleType.CENTER_CROP]. The crop anchor will be be nudged
     * so the entire cropped bitmap will fit inside the src. May return the input bitmap if no
     * scaling is necessary.
     *
     *
     *
     *
     * Example of changing verticalCenterPercent:
     * _________            _________
     * |         |          |         |
     * |         |          |_________|
     * |         |          |         |/___0.3f
     * |---------|          |_________|\
     * |         |<---0.5f  |         |
     * |---------|          |         |
     * |         |          |         |
     * |         |          |         |
     * |_________|          |_________|
     *
     * @param src original bitmap of any size
     * @param w desired width in px
     * @param h desired height in px
     * @param horizontalCenterPercent determines which part of the src to crop from. Range from 0
     * .0f to 1.0f. The value determines which part of the src
     * maps to the horizontal center of the resulting bitmap.
     * @param verticalCenterPercent determines which part of the src to crop from. Range from 0
     * .0f to 1.0f. The value determines which part of the src maps
     * to the vertical center of the resulting bitmap.
     * @return a copy of src conforming to the given width and height, or src itself if it already
     * matches the given width and height
     */
    fun crop(
        src: Bitmap?,
        w: Int,
        h: Int,
        horizontalCenterPercent: Float,
        verticalCenterPercent: Float,
        orientation: Int = 0
    ): Bitmap {
        if (horizontalCenterPercent < 0 || horizontalCenterPercent > 1 || verticalCenterPercent < 0 ||
            verticalCenterPercent > 1) {
            throw IllegalArgumentException(
                "horizontalCenterPercent and verticalCenterPercent must be between 0.0f and " + "1.0f, inclusive."
            )
        }
        val srcWidth = src!!.width
        val srcHeight = src.height
        // exit early if no resize/crop needed
        if (w == srcWidth && h == srcHeight) {
            return src
        }
        val m = Matrix()
        val scale = max(w.toFloat() / srcWidth, h.toFloat() / srcHeight)

        m.setScale(scale, scale)
        m.postRotate(orientation.toFloat())
        val srcCroppedW: Int
        val srcCroppedH: Int
        var srcX: Int
        var srcY: Int
        srcCroppedW = (w / scale).roundToInt()
        srcCroppedH = (h / scale).roundToInt()
        srcX = (srcWidth * horizontalCenterPercent - srcCroppedW / 2).toInt()
        srcY = (srcHeight * verticalCenterPercent - srcCroppedH / 2).toInt()
        // Nudge srcX and srcY to be within the bounds of src
        srcX = max(min(srcX, srcWidth - srcCroppedW), 0)
        srcY = max(min(srcY, srcHeight - srcCroppedH), 0)
        val cropped = Bitmap.createBitmap(
            src, srcX, srcY, srcCroppedW, srcCroppedH, m,
            true /* filter */
        )
        if (DEBUG)
            Timber.d(
                "IN centerCrop, srcW/H=%s/%s desiredW/H=%s/%s srcX/Y=%s/%s" + " innerW/H=%s/%s scale=%s resultW/H=%s/%s",
                srcWidth, srcHeight, w, h, srcX, srcY, srcCroppedW, srcCroppedH, scale,
                cropped.width, cropped.height
            )
        if (DEBUG && (w != cropped.width || h != cropped.height)) {
            Timber.e(Error(), "last center crop violated assumptions.")
        }
        return cropped
    }

    fun applyOverlay(
        sourceImage: Bitmap,
        overlayDrawableResourceId: Int
    ): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val width = sourceImage.width
            val height = sourceImage.height
            val r = context.resources

            val imageAsDrawable = BitmapDrawable(r, sourceImage)
            val layers = arrayOfNulls<Drawable>(2)

            layers[0] = imageAsDrawable
            layers[1] = BitmapDrawable(
                r,
                decodeSampledBitmapFromResource(
                    r,
                    overlayDrawableResourceId,
                    width,
                    height
                )
            )
            val layerDrawable = LayerDrawable(layers)
            bitmap = drawableToBitmap(layerDrawable)
        } catch (ex: Exception) {
        }

        return bitmap
    }

    fun decodeSampledBitmapFromResource(
        res: Resources,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap?

        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Returns an immutable bitmap from subset of source bitmap transformed by the given matrix.
     */
    fun createBitmap(bitmap: Bitmap, m: Matrix): Bitmap {
        // TODO: Re-implement createBitmap to avoid ARGB -> RBG565 conversion on platforms
        // prior to honeycomb.
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun closeStream(stream: Closeable?) {
        if (stream != null) {
            try {
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getBitmapBounds(uri: Uri): Rect {
        val bounds = Rect()
        var `is`: InputStream? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`is`, null, options)
            bounds.right = options.outWidth
            bounds.bottom = options.outHeight
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            closeStream(`is`)
        }
        return bounds
    }

    private fun getOrientation(uri: Uri): Int {
        var orientation = 0
        val cursor = context.contentResolver.query(uri, IMAGE_PROJECTION, null, null, null)
        if (cursor != null && cursor.moveToNext()) {
            orientation = cursor.getInt(INDEX_ORIENTATION)
        }
        return orientation
    }

    /**
     * Decodes immutable bitmap that keeps aspect-ratio and spans most within the given rectangle.
     */
    private fun decodeBitmap(uri: Uri, width: Int, height: Int): Bitmap? {
        var `is`: InputStream? = null
        var bitmap: Bitmap? = null
        try {
            // TODO: Take max pixels allowed into account for calculation to avoid possible OOM.
            val bounds = getBitmapBounds(uri)
            var sampleSize = Math.max(bounds.width() / width, bounds.height() / height)
            sampleSize = Math.min(
                sampleSize,
                Math.max(bounds.width() / height, bounds.height() / width)
            )
            val options = BitmapFactory.Options()
            options.inSampleSize = Math.max(sampleSize, 1)
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            `is` = context.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(`is`, null, options)
        } catch (e: FileNotFoundException) {
            Timber.e("FileNotFoundException: $uri")
        } finally {
            closeStream(`is`)
        }
        // Scale down the sampled bitmap if it's still larger than the desired dimension.
        if (bitmap != null) {
            var scale = Math.min(
                width.toFloat() / bitmap.width,
                height.toFloat() / bitmap.height
            )
            scale = Math.max(
                scale, Math.min(
                    height.toFloat() / bitmap.width,
                    width.toFloat() / bitmap.height
                )
            )
            if (scale < 1) {
                val m = Matrix()
                m.setScale(scale, scale)
                val transformed = createBitmap(bitmap, m)
                bitmap.recycle()
                return transformed
            }
        }
        return bitmap
    }

    /**
     * Gets decoded bitmap that keeps orientation as well.
     */
    fun getBitmap(uri: Uri, width: Int, height: Int): Bitmap? {
        val bitmap = decodeBitmap(uri, width, height)
        // Rotate the decoded bitmap according to its orientation if it's necessary.
        if (bitmap != null) {
            val orientation = getOrientation(uri)
            if (orientation != 0) {
                val m = Matrix()
                m.setRotate(orientation.toFloat())
                val transformed = createBitmap(bitmap, m)
                bitmap.recycle()
                return transformed
            }
        }
        return bitmap
    }

    /**
     * Saves the bitmap by given directory, filename, and format; if the directory is given null,
     * then saves it under the cache directory.
     */
    fun saveBitmap(
        bitmap: Bitmap,
        directory: String? = null,
        filename: String,
        format: CompressFormat = CompressFormat.JPEG
    ): File? {
        var directory = directory
        var filename = filename
        if (directory == null) {
            directory = context.cacheDir.absolutePath
        } else {
            // Check if the given directory exists or try to create it.
            val file = File(directory)
            if (!file.isDirectory && !file.mkdirs()) {
                return null
            }
        }
        var file: File? = null
        var os: OutputStream? = null
        try {
            filename = if (format == CompressFormat.PNG) "$filename.png" else "$filename.jpg"
            file = File(directory, filename)
            os = FileOutputStream(file)
            bitmap.compress(format, MAX_COMPRESS_QUALITY, os)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            closeStream(os)
        }
        return file
    }

    fun getCameraPhotoOrientation(imagePath: String): Int {
        var rotate = 0
        try {
            context.contentResolver.notifyChange(Uri.fromFile(File(imagePath)), null)
            val imageFile = File(imagePath)

            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }

            Timber.i("RotateImage Exif orientation: $orientation")
            Timber.i("RotateImage Rotate value: $rotate")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rotate
    }

    fun getCheckDepositPhotoOrientation(imagePath: String): Int {
        var rotate = 0
        try {
            // context.contentResolver.notifyChange(Uri.fromFile(File(imagePath)), null)
            val imageFile = File(imagePath)

            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }

            Timber.i("RotateImage Exif orientation: $orientation")
            Timber.i("RotateImage Rotate value: $rotate")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rotate
    }

    fun getAutoOrientation(imagePath: String): Float {
        var rotation = 0f
        try {
            // context.contentResolver.notifyChange(Uri.fromFile(File(imagePath)), null)`
            val imageFile = File(imagePath)
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            Timber.i("RotateImage Exif orientation: $orientation")
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    rotation = 270f
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    rotation = 180f
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    rotation = 90f
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rotation
    }

    fun scaleBitmap(bitmap: Bitmap, wantedWidth: Int, wantedHeight: Int): Bitmap {
        val output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val m = Matrix()
        m.setScale(wantedWidth.toFloat() / bitmap.width, wantedHeight.toFloat() / bitmap.height)
        canvas.drawBitmap(bitmap, m, Paint())
        return output
    }
}
