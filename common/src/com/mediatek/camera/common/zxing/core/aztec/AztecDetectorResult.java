/*
 * Copyright 2010 ZXing authors
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

package com.mediatek.camera.common.zxing.core.aztec;

import com.mediatek.camera.common.zxing.core.ResultPoint;
import com.mediatek.camera.common.zxing.core.common.BitMatrix;
import com.mediatek.camera.common.zxing.core.common.DetectorResult;

/**
 * <p>Extends {@link DetectorResult} with more information specific to the Aztec format,
 * like the number of layers and whether it's compact.</p>
 *
 * @author Sean Owen
 */
public final class AztecDetectorResult extends DetectorResult {

  private final boolean compact;
  private final int nbDatablocks;
  private final int nbLayers;

  public AztecDetectorResult(BitMatrix bits,
                             ResultPoint[] points,
                             boolean compact,
                             int nbDatablocks,
                             int nbLayers) {
    super(bits, points);
    this.compact = compact;
    this.nbDatablocks = nbDatablocks;
    this.nbLayers = nbLayers;
  }

  public int getNbLayers() {
    return nbLayers;
  }

  public int getNbDatablocks() {
    return nbDatablocks;
  }

  public boolean isCompact() {
    return compact;
  }

}
