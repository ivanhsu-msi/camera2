/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.camera.common.zxing.core;

/**
 * This object extends LuminanceSource around an array of YUV data returned from the camera driver,
 * with the option to crop to a rectangle within the full data. This can be used to exclude
 * superfluous pixels around the perimeter and speed up decoding.
 * <p>
 * It works for any pixel format where the Y channel is planar and appears first, including
 * YCbCr_420_SP and YCbCr_422_SP.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class PlanarYUVLuminanceSourceRotate extends LuminanceSource {

    private byte[] matrix;

    public PlanarYUVLuminanceSourceRotate(
            byte[] matrix,
            int width,
            int height) {
        super(height, width);
        this.matrix = matrix;
    }

    @Override
    public byte[] getRow(int x, byte[] row) {
        if (x < 0 || x >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + x);
        }
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        for (int i = 0; i < getWidth(); i++) {
            row[i] = matrix[i * getHeight() + x];
        }
        return row;
    }

    @Override
    public byte[] getMatrix() {
        return null;
    }

    @Override
    public boolean isCropSupported() {
        return true;
    }
}
