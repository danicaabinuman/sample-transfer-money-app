package com.unionbankph.corporate.app.common.widget.qrgenerator;

class ImageFrame {
    private final byte[] mData;
    private final int mWidth;
    private final int mHeight;

    ImageFrame(byte[] data, int width, int height, boolean rotate) {
        if (rotate) {
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
                }
            }
            int tmp = width;
            width = height;
            height = tmp;
            data = rotatedData;
        }
        this.mData = data;
        this.mWidth = width;
        this.mHeight = height;
    }

    byte[] getData() {
        return mData;
    }

    int getWidth() {
        return mWidth;
    }

    int getHeight() {
        return mHeight;
    }

    ImageFrame deepCopy() {
        byte[] data = new byte[mData.length];
        System.arraycopy(mData, 0, data, 0, mData.length);
        return new ImageFrame(data, mWidth, mHeight, false);
    }
}
