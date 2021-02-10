package com.unionbankph.corporate.app.common.widget.qrgenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import io.reactivex.Observable;

public final class RxQrCode {
    private static final int QR_CODE_LENGTH = 200;
    private static final int QR_CODE_IN_SAMPLE_LENGTH = 512;
    private static final int MAX_RETRY_TIMES = 4;

    private static final Map<DecodeHintType, ?> TRY_HARDER = Collections
            .singletonMap(DecodeHintType.TRY_HARDER, true);

    private RxQrCode() {
        // no instance
    }

    public static Observable<Result> scanFromPicture(String path) {
        return Observable.fromCallable(() -> {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            options.inSampleSize = calculateInSampleSize(options, QR_CODE_IN_SAMPLE_LENGTH,
                    QR_CODE_IN_SAMPLE_LENGTH);

            options.inJustDecodeBounds = false;
            int[] pixels = null;

            QRCodeReader reader = new QRCodeReader();
            int retryTimes = 0;
            Result result = null;
            while (result == null && retryTimes < MAX_RETRY_TIMES) {
                Bitmap picture = BitmapFactory.decodeFile(path, options);
                int width = picture.getWidth();
                int height = picture.getHeight();
                if (pixels == null) {
                    pixels = new int[picture.getWidth() * picture.getHeight()];
                }
                picture.getPixels(pixels, 0, width, 0, 0, width, height);
                picture.recycle();
                LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    result = reader.decode(bitmap, TRY_HARDER);
                } catch (NotFoundException | ChecksumException | FormatException ignored) {
                    retryTimes++;
                    options.inSampleSize *= 2;
                }
            }
            reader.reset();

            return result;
        }).flatMap(result -> {
            if (result == null) {
                return Observable.error(NotFoundException.getNotFoundInstance());
            }
            return Observable.just(result);
        });
    }


    public static Observable<File> generateQrCodeFile(Context context, String content, int width,
                                                      int height) {
        return Observable.create(emitter -> {
            MultiFormatWriter writer = new MultiFormatWriter();
            Bitmap origin = null;
            Bitmap scaled = null;
            try {
                BitMatrix bm = writer.encode(content, BarcodeFormat.QR_CODE, QR_CODE_LENGTH,
                        QR_CODE_LENGTH, Collections.singletonMap(EncodeHintType.MARGIN, 0));
                origin = Bitmap.createBitmap(QR_CODE_LENGTH, QR_CODE_LENGTH,
                        Bitmap.Config.ARGB_8888);

                for (int i = 0; i < QR_CODE_LENGTH; i++) {
                    for (int j = 0; j < QR_CODE_LENGTH; j++) {
                        origin.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                    }
                }
                scaled = Bitmap.createScaledBitmap(origin, width, height, true);
                File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (dir == null) {
                    emitter.onError(new IllegalStateException("external file system unavailable!"));
                    return;
                }
                String fileName = "rx_qr_" + System.currentTimeMillis() + ".png";
                File localFile = new File(dir, fileName);

                FileOutputStream outputStream = new FileOutputStream(localFile);
                scaled.compress(Bitmap.CompressFormat.PNG, 85, outputStream);
                outputStream.flush();
                outputStream.close();

                emitter.onNext(localFile);
                emitter.onComplete();
            } catch (WriterException | IOException e) {
                emitter.onError(e);
            } finally {
                if (origin != null) {
                    origin.recycle();
                }
                if (scaled != null) {
                    scaled.recycle();
                }
            }
        });
    }

    private static LuminanceSource frame2source(ImageFrame frame) {
        return new PlanarYUVLuminanceSource(frame.getData(),
                frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(),
                frame.getHeight(), false);
    }

    private static Observable<Result> resolve(LuminanceSource source, boolean failWhenNotFound) {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return Observable.just(reader.decode(bitmap, TRY_HARDER));
        } catch (NotFoundException | ChecksumException | FormatException e) {
            if (failWhenNotFound) {
                return Observable.error(e);
            }
        } finally {
            reader.reset();
        }
        return Observable.empty();
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                             int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        while (height / inSampleSize > reqHeight && width / inSampleSize > reqWidth) {
            inSampleSize *= 2;
        }

        return inSampleSize;
    }
}
