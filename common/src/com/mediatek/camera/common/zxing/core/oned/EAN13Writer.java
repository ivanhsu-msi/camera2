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

package com.mediatek.camera.common.zxing.core.oned;


import com.mediatek.camera.common.zxing.core.BarcodeFormat;
import com.mediatek.camera.common.zxing.core.FormatException;

import java.util.Collection;
import java.util.Collections;

/**
 * This object renders an EAN13 code as a {@link BitMatrix}.
 *
 * @author aripollak@gmail.com (Ari Pollak)
 */
public final class EAN13Writer extends UPCEANWriter {

  private static final int CODE_WIDTH = 3 + // start guard
      (7 * 6) + // left bars
      5 + // middle guard
      (7 * 6) + // right bars
      3; // end guard

  @Override
  protected Collection<BarcodeFormat> getSupportedWriteFormats() {
    return Collections.singleton(BarcodeFormat.EAN_13);
  }

  @Override
  public boolean[] encode(String contents) {
    int length = contents.length();
    switch (length) {
      case 12:
        // No check digit present, calculate it and add it
        int check;
        try {
          check = UPCEANReader.getStandardUPCEANChecksum(contents);
        } catch (FormatException fe) {
          throw new IllegalArgumentException(fe);
        }
        contents += check;
        break;
      case 13:
        try {
          if (!UPCEANReader.checkStandardUPCEANChecksum(contents)) {
            throw new IllegalArgumentException("Contents do not pass checksum");
          }
        } catch (FormatException ignored) {
          throw new IllegalArgumentException("Illegal contents");
        }
        break;
      default:
        throw new IllegalArgumentException(
            "Requested contents should be 12 or 13 digits long, but got " + length);
    }

    checkNumeric(contents);

    int firstDigit = Character.digit(contents.charAt(0), 10);
    int parities = EAN13Reader.FIRST_DIGIT_ENCODINGS[firstDigit];
    boolean[] result = new boolean[CODE_WIDTH];
    int pos = 0;

    pos += appendPattern(result, pos, UPCEANReader.START_END_PATTERN, true);

    // See EAN13Reader for a description of how the first digit & left bars are encoded
    for (int i = 1; i <= 6; i++) {
      int digit = Character.digit(contents.charAt(i), 10);
      if ((parities >> (6 - i) & 1) == 1) {
        digit += 10;
      }
      pos += appendPattern(result, pos, UPCEANReader.L_AND_G_PATTERNS[digit], false);
    }

    pos += appendPattern(result, pos, UPCEANReader.MIDDLE_PATTERN, false);

    for (int i = 7; i <= 12; i++) {
      int digit = Character.digit(contents.charAt(i), 10);
      pos += appendPattern(result, pos, UPCEANReader.L_PATTERNS[digit], true);
    }
    appendPattern(result, pos, UPCEANReader.START_END_PATTERN, true);

    return result;
  }

}
