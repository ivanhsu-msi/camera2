/*
 * Copyright 2007 ZXing authors
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

package com.mediatek.camera.common.zxing.core.qrcode;

import com.mediatek.camera.common.zxing.Config;
import com.mediatek.camera.common.zxing.core.BarcodeFormat;
import com.mediatek.camera.common.zxing.core.BinaryBitmap;
import com.mediatek.camera.common.zxing.core.ChecksumException;
import com.mediatek.camera.common.zxing.core.DecodeHintType;
import com.mediatek.camera.common.zxing.core.FormatException;
import com.mediatek.camera.common.zxing.core.NotFoundException;
import com.mediatek.camera.common.zxing.core.Reader;
import com.mediatek.camera.common.zxing.core.Result;
import com.mediatek.camera.common.zxing.core.ResultMetadataType;
import com.mediatek.camera.common.zxing.core.ResultPoint;
import com.mediatek.camera.common.zxing.core.common.BitMatrix;
import com.mediatek.camera.common.zxing.core.common.DecoderResult;
import com.mediatek.camera.common.zxing.core.common.DetectorResult;
import com.mediatek.camera.common.zxing.core.qrcode.decoder.Decoder;
import com.mediatek.camera.common.zxing.core.qrcode.decoder.QRCodeDecoderMetaData;
import com.mediatek.camera.common.zxing.core.qrcode.detector.Detector;
import com.mediatek.camera.common.zxing.core.qrcode.detector.Detector2;

import java.util.List;
import java.util.Map;

/**
 * This implementation can detect and decode QR Codes in an image.
 *
 * @author Sean Owen
 */
public class QRCodeReader implements Reader {

    private static final ResultPoint[] NO_POINTS = new ResultPoint[0];

    private final Decoder decoder = new Decoder();

    protected final Decoder getDecoder() {
        return decoder;
    }

    /**
     * Locates and decodes a QR code in an image.
     *
     * @return a String representing the content encoded by the QR code
     * @throws NotFoundException if a QR code cannot be found
     * @throws FormatException   if a QR code cannot be decoded
     * @throws ChecksumException if error correction fails
     */
    @Override
    public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
        return decode(image, null);
    }

    @Override
    public final Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints)
            throws ChecksumException, FormatException, NotFoundException {
        DecoderResult decoderResult;
        ResultPoint[] points;
        if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
            BitMatrix bits;
            try {
                bits = extractPureBits(image.getBlackMatrix());
            } catch (NotFoundException e) {
                return null;
            }
            decoderResult = decoder.decode(bits, hints);
            points = NO_POINTS;
        } else {
            DetectorResult detectorResult;
            try {
                detectorResult = new Detector(image.getBlackMatrix()).detect(hints);
            } catch (NotFoundException e) {
                if (Config.isSupportBlackEdge) {
                    try {
                        detectorResult = new Detector2(image.getBlackMatrix()).detect(hints);
                    } catch (NotFoundException notFoundException) {
                        throw NotFoundException.getNotFoundInstance();
                    }
                } else
                    throw NotFoundException.getNotFoundInstance();
            }
            points = detectorResult.getPoints();
            try {
                decoderResult = decoder.decode(detectorResult.getBits(), hints);
            } catch (Exception ignored) {
                return new Result(null, null, points, null);
            }
        }
        // If the code was mirrored: swap the bottom-left and the top-right points.
        if (decoderResult.getOther() instanceof QRCodeDecoderMetaData) {
            ((QRCodeDecoderMetaData) decoderResult.getOther()).applyMirroredCorrection(points);
        }

        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE);
        List<byte[]> byteSegments = decoderResult.getByteSegments();
        if (byteSegments != null) {
            result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
        }
        String ecLevel = decoderResult.getECLevel();
        if (ecLevel != null) {
            result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
        }
        if (decoderResult.hasStructuredAppend()) {
            result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE,
                    decoderResult.getStructuredAppendSequenceNumber());
            result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_PARITY,
                    decoderResult.getStructuredAppendParity());
        }
        return result;
    }

    @Override
    public void reset() {
        // do nothing
    }

    /**
     * This method detects a code in a "pure" image -- that is, pure monochrome image
     * which contains only an unrotated, unskewed, image of a code, with some white border
     * around it. This is a specialized method that works exceptionally fast in this special
     * case.
     */
    private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {

        int[] leftTopBlack = image.getTopLeftOnBit();
        int[] rightBottomBlack = image.getBottomRightOnBit();
        if (leftTopBlack == null || rightBottomBlack == null) {
            throw NotFoundException.getNotFoundInstance();
        }

        float moduleSize = moduleSize(leftTopBlack, image);

        int top = leftTopBlack[1];
        int bottom = rightBottomBlack[1];
        int left = leftTopBlack[0];
        int right = rightBottomBlack[0];

        // Sanity check!
        if (left >= right || top >= bottom) {
            throw NotFoundException.getNotFoundInstance();
        }

        if (bottom - top != right - left) {
            // Special case, where bottom-right module wasn't black so we found something else in the last row
            // Assume it's a square, so use height as the width
            right = left + (bottom - top);
            if (right >= image.getWidth()) {
                // Abort if that would not make sense -- off image
                throw NotFoundException.getNotFoundInstance();
            }
        }

        int matrixWidth = Math.round((right - left + 1) / moduleSize);
        int matrixHeight = Math.round((bottom - top + 1) / moduleSize);
        if (matrixWidth <= 0 || matrixHeight <= 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (matrixHeight != matrixWidth) {
            // Only possibly decode square regions
            throw NotFoundException.getNotFoundInstance();
        }

        // Push in the "border" by half the module width so that we start
        // sampling in the middle of the module. Just in case the image is a
        // little off, this will help recover.
        int nudge = (int) (moduleSize / 2.0f);
        top += nudge;
        left += nudge;

        // But careful that this does not sample off the edge
        // "right" is the farthest-right valid pixel location -- right+1 is not necessarily
        // This is positive by how much the inner x loop below would be too large
        int nudgedTooFarRight = left + (int) ((matrixWidth - 1) * moduleSize) - right;
        if (nudgedTooFarRight > 0) {
            if (nudgedTooFarRight > nudge) {
                // Neither way fits; abort
                throw NotFoundException.getNotFoundInstance();
            }
            left -= nudgedTooFarRight;
        }
        // See logic above
        int nudgedTooFarDown = top + (int) ((matrixHeight - 1) * moduleSize) - bottom;
        if (nudgedTooFarDown > 0) {
            if (nudgedTooFarDown > nudge) {
                // Neither way fits; abort
                throw NotFoundException.getNotFoundInstance();
            }
            top -= nudgedTooFarDown;
        }

        // Now just read off the bits
        BitMatrix bits = new BitMatrix(matrixWidth, matrixHeight);
        for (int y = 0; y < matrixHeight; y++) {
            int iOffset = top + (int) (y * moduleSize);
            for (int x = 0; x < matrixWidth; x++) {
                if (image.get(left + (int) (x * moduleSize), iOffset)) {
                    bits.set(x, y);
                }
            }
        }
        return bits;
    }

    private static float moduleSize(int[] leftTopBlack, BitMatrix image) throws
            NotFoundException {
        int height = image.getHeight();
        int width = image.getWidth();
        int x = leftTopBlack[0];
        int y = leftTopBlack[1];
        boolean inBlack = true;
        int transitions = 0;
        while (x < width && y < height) {
            if (inBlack != image.get(x, y)) {
                if (++transitions == 5) {
                    break;
                }
                inBlack = !inBlack;
            }
            x++;
            y++;
        }
        if (x == width || y == height) {
            throw NotFoundException.getNotFoundInstance();
        }
        return (x - leftTopBlack[0]) / 7.0f;
    }

}
