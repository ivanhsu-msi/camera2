/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.common.loader;

import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Size;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Device description used to describe one camera's info, include:
 * 1.Camera info.
 * 2.Camera characteristics.
 * 3.Camera parameters.
 */
public class DeviceDescription {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(DeviceDescription.class.getSimpleName());
    private final CameraInfo mCameraInfo;
    private CameraCharacteristics mCameraCharacteristics;
    private Parameters mParameters;

    // store hdr camera characteristics for launch performance.
    private static final String HDR_KEY_AVAILABLE_HDR_MODES_PHOTO =
            "com.mediatek.hdrfeature.availableHdrModesPhoto";
    private static final String VSDOF_KEY_OPTICAL_ZOOM_SETS =
            "com.mediatek.vsdoffeature.vsdofFeatureSupportedOpticalZoomSets";
    private static final String HDR_KEY_AVAILABLE_HDR_MODES_VIDEO =
            "com.mediatek.hdrfeature.availableHdrModesVideo";
    private static final String VSDOF_FEATURE_SUPPORT_VIDEO_FPS =
            "com.mediatek.vsdoffeature.vsdofFeatureSupportedVideoFps";
    private static final String HDR_KEY_DETECTION_RESULT =
            "com.mediatek.hdrfeature.hdrDetectionResult";
    private static final String HDR_KEY_DETECTION_MODE =
            "com.mediatek.hdrfeature.hdrMode";
    private static final String HDR_KEY_SESSION_MODE =
            "com.mediatek.hdrfeature.SessionParamhdrMode";
    private static final String FPS60_KEY_SESSION_PARAMETER =
            "com.mediatek.streamingfeature.hfpsMode";
    private static final String EIS_KEY_SESSION_PARAMETER =
            "com.mediatek.eisfeature.eismode";
    private static final String PREVIEW_EIS_PARAMETER =
            "com.mediatek.eisfeature.previeweis";
    private static final String THUMBNAIL_KEY_AVAILABLE_MODES =
            "com.mediatek.control.capture.availablepostviewmodes";
    private static final String THUMBNAIL_KEY_POSTVIEW_SIZE =
            "com.mediatek.control.capture.postviewsize";
    private static final String AIS_AVAILABLE_MODES_KEY_NAME
            = "com.mediatek.mfnrfeature.availablemfbmodes";
    private static final String AIS_REQUEST_MODE_KEY_NAME
            = "com.mediatek.mfnrfeature.mfbmode";
    private static final String AIS_RESULT_MODE_KEY_NAME
            = "com.mediatek.mfnrfeature.mfbresult";
    private static final String ISO_KEY_CONTROL_SPEED
            = "com.mediatek.3afeature.aeIsoSpeed";
    private static final String FLASH_KEY_CUSTOMIZED_RESULT =
            "com.mediatek.flashfeature.customizedResult";
    private static final String FLASH_KEY_CUSTOMIZED_COLOR =
            "com.mediatek.3afeature.awbCct";
    private static final String FLASH_KEY_CUSTOMIZATION_AVAILABLE =
            "com.mediatek.flashfeature.customization.available";
    private static final String CS_KEY_AVAILABLE_MODES =
            "com.mediatek.cshotfeature.availableCShotModes";
    private static final String CS_KEY_CAPTURE_REQUEST =
            "com.mediatek.cshotfeature.capture";
    private static final String P2_KEY_SUPPORT_MODES =
            "com.mediatek.control.capture.early.notification.support";
    private static final String P2_KEY_NOTIFICATION_TRIGGER =
            "com.mediatek.control.capture.early.notification.trigger";
    private static final String P2_KEY_NOTIFICATION_RESULT =
            "com.mediatek.control.capture.next.ready";
    private static final String ASD_AVAILABLE_MODES_KEY_NAME
            = "com.mediatek.facefeature.availableasdmodes";
    private static final String ASD_REQUEST_MODE_KEY_NAME
            = "com.mediatek.facefeature.asdmode";
    private static final String ASD_RESULT_MODE_KEY_NAME
            = "com.mediatek.facefeature.asdresult";
    private static final String ZSL_KEY_AVAILABLE_MODES =
            "com.mediatek.control.capture.available.zsl.modes";
    private static final String ZSL_KEY_MODE_REQUEST =
            "com.mediatek.control.capture.zsl.mode";
    private static final String FLASH_CALIBRATION_AVAILABLE
            = "com.mediatek.flashfeature.calibration.available";
    private static final String FLASH_CALIBRATION_REQUEST_KEY =
            "com.mediatek.flashfeature.calibration.enable";
    private static final String FLASH_CALIBRATION_STATE_KEY =
            "com.mediatek.flashfeature.calibration.state";
    private static final String FLASH_CALIBRATION_RESULT_KEY_NAME
            = "com.mediatek.flashfeature.calibration.result";
    private static final String BG_SERVICE_AVAILABLE_MODES =
            "com.mediatek.bgservicefeature.availableprereleasemodes";
    private static final String BG_SERVICE_PRERELEASE =
            "com.mediatek.bgservicefeature.prerelease";
    private static final String BG_SERVICE_IMAGEREADER_ID =
            "com.mediatek.bgservicefeature.imagereaderid";
    private static final String SMVR_AVAILABLE_MODES =
            "com.mediatek.smvrfeature.availableSmvrModes";
    private static final String SMVR_V2_AVAILABLE_MODES =
            "com.mediatek.smvrfeature.availableSmvrV2Modes";
    private static final String SMVR_REQUEST_MODE =
            "com.mediatek.smvrfeature.smvrMode";
    private static final String SMVR_V2_REQUEST_MODE =
            "com.mediatek.smvrfeature.smvrV2Mode";
    private static final String SMVR_RESULT_BURST =
            "com.mediatek.smvrfeature.smvrResult";
    private static final String SMVR_V2_RESULT_BURST =
            "com.mediatek.smvrfeature.smvrV2Result";
    private static final String VSDOF_KEY =
            "com.mediatek.multicamfeature.multiCamFeatureMode";
    private static final String VSDOF_ZOOM_SET =
            "com.mediatek.vsdoffeature.vsdofFeatureOpticalZoomSet";
    private static final String HDR10_KEY
            = "com.mediatek.streamingfeature.hdr10";
    private static final String MSHDR_AVAILABLE_MODE =
            "com.mediatek.hdrfeature.availableMStreamHdrModes";
    private static final String STAGGERHDR_AVAILABLE_MODE =
            "com.mediatek.hdrfeature.availableStaggerHdrModes";
    private static final String PLATFORM_CAMERA_KEY =
            "com.mediatek.configure.setting.proprietaryRequest";
    private static final String MTK_MULTI_CAM_FEATURE_AVAILABLE_MODE
            = "com.mediatek.multicamfeature.availableMultiCamFeatureMode";
    private static final String MTK_LENS_INFO_AVAILABLE_FOCAL_LENGTHS
            = "android.lens.info.availableFocalLengths";
    private static final String MTK_STREAMING_FEATURE_AVAILABLE_HFPS_MAX_RESOLUTIONS
            = "com.mediatek.streamingfeature.availableHfpsMaxResolutions";
    private static final String MTK_STREAMING_FEATURE_AVAILABLE_HFPS_EIS_MAX_RESOLUTIONS
            = "com.mediatek.streamingfeature.availableHfpsEISMaxResolutions";
    private static final String MTK_HEIC_INFO_SUPPORTED
            ="android.heic.info.supported";
    private static final String MTK_MULTICAM_FEATURE_MULTI_CAM_AF_ROI =
            "com.mediatek.multicamfeature.multiCamAfRoi";
    private static final String MTK_MULTICAM_FEATURE_MULTI_CAM_AE_ROI  =
            "com.mediatek.multicamfeature.multiCamAeRoi";
    private static final String TPI_SUPPORT_VALUE_KEY
            = "com.mediatek.streamingfeature.tpiSupportValue";
    private static final String TPI_REQUEST_KEY =
            "com.mediatek.streamingfeature.tpiRequestKey";

    private static final String MTK_MULTI_CAM_CONFIG_SCALER_CROP_REGION
            = "com.mediatek.multicamfeature.multiCamConfigScalerCropRegion";
    private static final String TRACKING_AF_SUPPORT
            = "com.mediatek.trackingaffeature.trackingafAvailableModes";
    private static final String HDR10_VSS_SUPPORT
            = "com.mediatek.streamingfeature.hdr10pVssSupport";
    private static final String HDR10_EIS_SUPPORT
            = "com.mediatek.streamingfeature.hdr10pEisSupport";
    private static final String MTK_VIDEO_AINR_SUPPORT
            = "com.mediatek.videoainrfeature.videoAinrAvailableModes";
    private static final String MTK_VIDEO_AINR_MODE
            = "com.mediatek.videoainrfeature.videoAinrModes";
    private static final String MTK_VIDEO_AINR_SUPPORT_SIZE
            = "com.mediatek.videoainrfeature.videoAinrSupportedSizes";
    private static final String MTK_VIDEO_AINR_JOB_STATUS
            = "com.mediatek.videoainrfeature.videoAinrOnJobStatus";
    private static final String MTK_CAMERA_PREVIEW_COMPRESSION_MODES
            = "com.mediatek.camerapreviewcompression.CameraPreviewCompressionModes";
    private static final String MTK_CAMERA_PREVIEW_COMPRESSION
            = "com.mediatek.camerapreviewcompression.CameraPreviewCompression";

    //for zoommanualing
    private static final String ZMMANUALING_REQUEST_RATIO_KEY =
            "com.mediatek.3afeature.afZoomRatioValue";
    private static final String ZMMANUALING_REQUEST_STOP =
            "com.mediatek.3afeature.afZoomStop";
    private static final String ZMMANUALING_MODES =
            "com.mediatek.3afeature.availableOpZoomModes";
    private static final int POSTVIEW_SUPPORT = 1;
    private static final int MTK_MULTI_CAM_FEATURE_MODE_VSDOF = 1;
    private static final int MTK_MULTI_CAM_FEATURE_MODE_DUAL_ZOOM = 0;
    private boolean mZslSupport;
    private boolean mCshotSupport;
    private boolean mSpeedUpSupported;
    private boolean mThumbnailPostViewSupport;
    private boolean mIsFlashCalibrationSupported;
    private boolean mIsFlashCustomizedAvailable;
    private boolean mBGServiceSupport;
    private boolean mStereoModeSupport;
    private boolean mDualZoomSupport;
    private boolean mFocalLengthSupport;
    private boolean mHeicInfoSupported;
    private boolean mTrackingAfSupported;
    private boolean mHDR10EisSupprot;
    private boolean mHDR10VssSupprot;
    private boolean mFps60SdofSupprot;
    private boolean mAinrSupprot;

    private ArrayList<Size> mKeyThumbnailSizes = new ArrayList<>();

    private CameraCharacteristics.Key<int[]> mKeyHdrAvailablePhotoModes;
    private CameraCharacteristics.Key<int[]> mKeyVsdofKeyOpticalZoomSets;
    private CameraCharacteristics.Key<int[]> mKeyHdrAvailableVideoModes;
    private CameraCharacteristics.Key<int[]> mKeyThumbnailAvailableModes;
    private CameraCharacteristics.Key<int[]> mKeyAisAvailableModes;
    private CameraCharacteristics.Key<int[]> mKeyAsdAvailableModes;
    private CameraCharacteristics.Key<int[]> mKeySMVRAvailableModes;
    private CameraCharacteristics.Key<int[]> mKeySMVRV2AvailableModes;
    private CameraCharacteristics.Key<int[]> mKeyMSHDRMode;
    private CameraCharacteristics.Key<int[]> mKeyStaggerHDRMode;
    private CameraCharacteristics.Key<int[]> mKeyAvaliableHfpsMaxResolutions;
    private CameraCharacteristics.Key<int[]> mKeyAvaliableHfpsEisMaxResolutions;
    private CameraCharacteristics.Key<int[]> mKeyTpiSupportValue;
    private CameraCharacteristics.Key<int[]> mKeyPreviewCompressionSupportModes;
    private CaptureResult.Key<int[]> mKeyHdrDetectionResult;
    private CaptureResult.Key<int[]> mKeyAisResult;
    private CaptureResult.Key<byte[]> mKeyFlashCustomizedResult;
    private CaptureResult.Key<int[]> mKeyFlashCustomizedColorResult;
    private CaptureResult.Key<int[]> mKeyP2NotificationResult;
    private CaptureResult.Key<int[]> mKeyAsdResult;
    private CaptureResult.Key<int[]> mKeySMVRBurstResult;
    private CaptureResult.Key<int[]> mKeySMVRV2BurstResult;
    private CaptureResult.Key<int[]> mKeyFlashCalibrationState;
    private CaptureRequest.Key<int[]> mKeyHdrRequestMode;
    private CaptureRequest.Key<int[]> mKeyHdrRequsetSessionMode;
    private CaptureRequest.Key<int[]> mKeyEisSessionParameter;
    private CaptureRequest.Key<int[]> mKeyFps60SessionParameter;
    private CaptureRequest.Key<int[]> mKeyPreviewEisParameter;
    private CaptureRequest.Key<int[]> mKeyAisRequestMode;
    private CaptureRequest.Key<int[]> mKeyIsoRequestMode;
    private CaptureRequest.Key<int[]> mKeyCshotRequestMode;
    private CaptureRequest.Key<int[]> mKeyP2NotificationRequestMode;
    private CaptureRequest.Key<int[]> mKeyAsdRequestMode;
    private CaptureRequest.Key<int[]> mKeyPostViewRequestSizeMode;
    private CaptureRequest.Key<byte[]> mKeyZslMode;
    private CaptureRequest.Key<int[]> mKeyFlashCalibrationRequest;
    private CaptureResult.Key<int[]> mKeyFlashCalibrationResult;
    private CaptureRequest.Key<int[]> mKeyBGServicePrerelease;
    private CaptureRequest.Key<int[]> mKeyBGServiceImagereaderId;
    private CaptureRequest.Key<int[]> mKeySMVRRequestMode;
    private CaptureRequest.Key<int[]> mKeySMVRV2RequestMode;
    private CaptureRequest.Key<int[]> mKeyVsdof;
    private CaptureRequest.Key<int[]> mKeyVsdofZoomSet;
    private CaptureRequest.Key<int[]> mKeyHdr10;
    private CaptureRequest.Key<int[]> mKeyPlatformCamera;
    private CaptureRequest.Key<int[]> mKeyMultiCamAfRoi;
    private CaptureRequest.Key<int[]> mKeyMultiCamAeRoi;
    private CaptureRequest.Key<int[]> mKeyTpiRequestValue;
    private CaptureRequest.Key<int[]> mKeyMultiCamConfigScalerCropRegion;
    private CaptureRequest.Key<int[]> mKeyAinrRequsetSessionMode;
    private CaptureRequest.Key<int[]> mKeyPreviewCompressionRequest;
    //zoommanualing
    private CaptureRequest.Key<int[]> mKeyZoomManualingRequestRatio;
    private boolean mZoomManualingSupport = false;

    /**
     * Construct a camera device description.
     *
     * @param cameraInfo the camera info.
     */
    public DeviceDescription(@Nonnull CameraInfo cameraInfo) {
        mCameraInfo = cameraInfo;
    }

    /**
     * Set this camera's characteristics.
     *
     * @param cameraCharacteristics this camera's characteristics.
     */
    public void setCameraCharacteristics(@Nonnull CameraCharacteristics
                                                 cameraCharacteristics) {
        mCameraCharacteristics = cameraCharacteristics;
    }

    /**
     * Set this camera's parameters.
     *
     * @param parameters this camera's parameters.
     */
    public void setParameters(@Nonnull Parameters parameters) {
        mParameters = parameters;
    }

    /**
     * Get this camera's info.
     *
     * @return this camera's info.
     */
    public CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    /**
     * Get this camera's characteristics, if not set this will return null.
     *
     * @return this camera's characteristics.
     */
    public CameraCharacteristics getCameraCharacteristics() {
        return mCameraCharacteristics;
    }

    /**
     * Get this camera's parameters, if not set this will return null.
     *
     * @return this camera's parameters.
     */
    public Parameters getParameters() {
        return mParameters;
    }

    /**
     * Store camera vendor keys.
     *
     * @param cs The cameraCharacteristics of according to camera id.
     */
    public void storeCameraCharacKeys(CameraCharacteristics cs) {
        Size[] thumbnailSizes = cs.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES);
        for (Size s : thumbnailSizes) {
            mKeyThumbnailSizes.add(s);
        }

        List<CameraCharacteristics.Key<?>> keyList = cs.getKeys();
        for (CameraCharacteristics.Key<?> key : keyList) {
            if (key.getName().equals(HDR_KEY_AVAILABLE_HDR_MODES_PHOTO)) {
                mKeyHdrAvailablePhotoModes = (CameraCharacteristics.Key<int[]>) key;
            } else if(key.getName().equals(VSDOF_KEY_OPTICAL_ZOOM_SETS)){
                mKeyVsdofKeyOpticalZoomSets = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(HDR_KEY_AVAILABLE_HDR_MODES_VIDEO)) {
                mKeyHdrAvailableVideoModes = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(VSDOF_FEATURE_SUPPORT_VIDEO_FPS)) {
                CameraCharacteristics.Key<int[]> mKeyVsdofFeatureSupportVideoFps = (CameraCharacteristics.Key<int[]>) key;
                int[] fpsModes = cs.get(mKeyVsdofFeatureSupportVideoFps);
                if (fpsModes != null) {
                    for (int mode : fpsModes) {
                        if (mode == 60) {
                            mFps60SdofSupprot = true;
                            break;
                        }
                    }
                }
            } else if (key.getName().equals(THUMBNAIL_KEY_AVAILABLE_MODES)) {
                mKeyThumbnailAvailableModes = (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(mKeyThumbnailAvailableModes);
                if (availableModes != null) {
                    for (int mode : availableModes) {
                        if (mode == POSTVIEW_SUPPORT) {
                            mThumbnailPostViewSupport = true;
                            break;
                        }
                    }
                }
            } else if (key.getName().equals(AIS_AVAILABLE_MODES_KEY_NAME)) {
                mKeyAisAvailableModes = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(FLASH_KEY_CUSTOMIZATION_AVAILABLE)) {
                CameraCharacteristics.Key<byte[]> availableValue =
                        (CameraCharacteristics.Key<byte[]>) key;
                byte[] availableValues = cs.get(availableValue);
                for (byte value : availableValues) {
                    if (value == 1) {
                        mIsFlashCustomizedAvailable = true;
                        break;
                    }
                }
            } else if (key.getName().equals(CS_KEY_AVAILABLE_MODES)) {
                CameraCharacteristics.Key<int[]> availableMode =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(availableMode);
                for (int value : availableModes) {
                    if (value == 1) {
                        mCshotSupport = true;
                        break;
                    }
                }
            } else if (key.getName().equals(P2_KEY_SUPPORT_MODES)) {
                CameraCharacteristics.Key<int[]> availableMode =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(availableMode);
                for (int value : availableModes) {
                    if (value == 1) {
                        mSpeedUpSupported = true;
                        break;
                    }
                }
            } else if (key.getName().equals(ASD_AVAILABLE_MODES_KEY_NAME)) {
                mKeyAsdAvailableModes = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(ZSL_KEY_AVAILABLE_MODES)) {
                CameraCharacteristics.Key<byte[]> availableMode =
                        (CameraCharacteristics.Key<byte[]>) key;
                byte[] availableModes = cs.get(availableMode);
                ////TODO: null pointer
                for (byte value : availableModes) {
                    if (value == 1) {
                        mZslSupport = true;
                        break;
                    }
                }
            } else if (key.getName().equals(FLASH_CALIBRATION_AVAILABLE)) {
                CameraCharacteristics.Key<int[]> availableKey =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableValues = cs.get(availableKey);
                for (int value : availableValues) {
                    if (value == 1) {
                        mIsFlashCalibrationSupported = true;
                        break;
                    }
                }
            } else if (key.getName().equals(BG_SERVICE_AVAILABLE_MODES)) {
                CameraCharacteristics.Key<int[]> availableMode =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(availableMode);
                for (int value : availableModes) {
                    if (value == 1) {
                        mBGServiceSupport = true;
                        break;
                    }
                }
            } else if (key.getName().equals(SMVR_V2_AVAILABLE_MODES)) {
                mKeySMVRV2AvailableModes = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(SMVR_AVAILABLE_MODES)) {
                mKeySMVRAvailableModes = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(MSHDR_AVAILABLE_MODE)) {
                mKeyMSHDRMode = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(STAGGERHDR_AVAILABLE_MODE)) {
                mKeyStaggerHDRMode = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(MTK_MULTI_CAM_FEATURE_AVAILABLE_MODE)) {
                CameraCharacteristics.Key<int[]> availableMode =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(availableMode);
                for (int value : availableModes) {
                    if (value == MTK_MULTI_CAM_FEATURE_MODE_VSDOF) {
                        mStereoModeSupport = true;
                    }
                    if (value == MTK_MULTI_CAM_FEATURE_MODE_DUAL_ZOOM) {
                        mDualZoomSupport = true;
                    }
                }
            } else if (key.getName().equals(MTK_LENS_INFO_AVAILABLE_FOCAL_LENGTHS)) {
                CameraCharacteristics.Key<float[]> availableMode =
                        (CameraCharacteristics.Key<float[]>) key;
                float[] availableModes = cs.get(availableMode);
                if (availableModes.length == 1) {
                    mFocalLengthSupport = true;
                }
            } else if (key.getName().equals(MTK_STREAMING_FEATURE_AVAILABLE_HFPS_MAX_RESOLUTIONS)) {
                mKeyAvaliableHfpsMaxResolutions = (CameraCharacteristics.Key<int[]>) key;

            } else if (key.getName().equals(MTK_STREAMING_FEATURE_AVAILABLE_HFPS_EIS_MAX_RESOLUTIONS)) {
                mKeyAvaliableHfpsEisMaxResolutions = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(MTK_HEIC_INFO_SUPPORTED)) {
                CameraCharacteristics.Key<byte[]> availableMode =
                        (CameraCharacteristics.Key<byte[]>) key;
                byte[] availableModes = cs.get(availableMode);
                for (byte value : availableModes) {
                    LogHelper.d(TAG, "[storeCameraCharacKeys] MTK_HEIC_INFO_SUPPORTED " +value);
                    if (value == 1) {
                        mHeicInfoSupported = true;
                        break;
                    }
                }
            }  else if (key.getName().equals(TRACKING_AF_SUPPORT)) {
                LogHelper.d(TAG, "[storeCameraCharacKeys] TRACKING_AF_SUPPORT");

                CameraCharacteristics.Key<int[]> availableMode =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(availableMode);
                for (int value : availableModes) {
                    if (value == 1) {
                        mTrackingAfSupported = true;
                        break;
                    }
                }
            }else if (key.getName().equals(TPI_SUPPORT_VALUE_KEY)){
                mKeyTpiSupportValue = (CameraCharacteristics.Key<int[]>) key;
            }else if(key.getName().equals(HDR10_EIS_SUPPORT)){
                CameraCharacteristics.Key<int[]> availableValue =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(availableValue);
                for (int value : availableModes) {
                    if (value == 1) {
                        mHDR10EisSupprot = true;
                        break;
                    }
                }
            }else if(key.getName().equals(HDR10_VSS_SUPPORT)){
                CameraCharacteristics.Key<int[]> availableValue =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(availableValue);
                for (int value : availableModes) {
                    if (value == 1) {
                        mHDR10VssSupprot = true;
                        break;
                    }
                }
            }else if(key.getName().equals(MTK_VIDEO_AINR_SUPPORT)){
                CameraCharacteristics.Key<int[]> availableValue =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(availableValue);
                for (int value : availableModes) {
                    LogHelper.i(TAG, "[MTK_VIDEO_AINR_SUPPORT]" + value);
                    if (value==1) {
                        mAinrSupprot = true;
                        break;
                    }
                }
            }else if(key.getName().equals(MTK_CAMERA_PREVIEW_COMPRESSION_MODES)){
                mKeyPreviewCompressionSupportModes = (CameraCharacteristics.Key<int[]>) key;
            } else if (key.getName().equals(ZMMANUALING_MODES)) {
                CameraCharacteristics.Key<int[]> availableMode =
                        (CameraCharacteristics.Key<int[]>) key;
                int[] availableModes = cs.get(availableMode);
                for (int value : availableModes) {
                    LogHelper.d(TAG, "[storeCameraCharacKeys] ZMMANUALING_MODES " +value);
                    if (value == 1) {
                        mZoomManualingSupport = true;
                        break;
                    }
                }
            }
        }

        List<CaptureRequest.Key<?>> sessionKeyList = cs.getAvailableSessionKeys();
        for (CaptureRequest.Key<?> requestKey : sessionKeyList) {
            if (requestKey.getName().equals(VSDOF_KEY)) {
                mKeyVsdof = (CaptureRequest.Key<int[]>) requestKey;
            }
            if (requestKey.getName().equals(VSDOF_ZOOM_SET)) {
                mKeyVsdofZoomSet = (CaptureRequest.Key<int[]>) requestKey;
            }
            if (requestKey.getName().equals(HDR10_KEY)) {
                mKeyHdr10 = (CaptureRequest.Key<int[]>) requestKey;
            }
            if (MTK_MULTI_CAM_CONFIG_SCALER_CROP_REGION.equals(requestKey.getName())){
                mKeyMultiCamConfigScalerCropRegion = (CaptureRequest.Key<int[]>)requestKey;
            }
            if(MTK_VIDEO_AINR_MODE.equals(requestKey.getName())){
                mKeyAinrRequsetSessionMode =  (CaptureRequest.Key<int[]>)requestKey;
            }else {
                LogHelper.i(TAG, "[requestKey.getName()]" + requestKey.getName());
            }
        }

        List<CaptureResult.Key<?>> resultKeyList = cs.getAvailableCaptureResultKeys();
        for (CaptureResult.Key<?> resultKey : resultKeyList) {
            if (resultKey.getName().equals(HDR_KEY_DETECTION_RESULT)) {
                mKeyHdrDetectionResult = (CaptureResult.Key<int[]>) resultKey;
            } else if (resultKey.getName().equals(AIS_RESULT_MODE_KEY_NAME)) {
                mKeyAisResult = (CaptureResult.Key<int[]>) resultKey;
            } else if (resultKey.getName().equals(FLASH_KEY_CUSTOMIZED_RESULT)) {
                mKeyFlashCustomizedResult = (CaptureResult.Key<byte[]>) resultKey;
            } else if (resultKey.getName().equals(P2_KEY_NOTIFICATION_RESULT)) {
                mKeyP2NotificationResult = (CaptureResult.Key<int[]>) resultKey;
            } else if (resultKey.getName().equals(ASD_RESULT_MODE_KEY_NAME)) {
                mKeyAsdResult = (CaptureResult.Key<int[]>) resultKey;
            } else if (resultKey.getName().equals(FLASH_CALIBRATION_RESULT_KEY_NAME)) {
                mKeyFlashCalibrationResult = (CaptureResult.Key<int[]>) resultKey;
            }else if (resultKey.getName().equals(SMVR_V2_RESULT_BURST)) {
                mKeySMVRV2BurstResult = (CaptureResult.Key<int[]>) resultKey;
            } else if (resultKey.getName().equals(SMVR_RESULT_BURST)) {
                mKeySMVRBurstResult = (CaptureResult.Key<int[]>) resultKey;
            } else if (resultKey.getName().equals(FLASH_KEY_CUSTOMIZED_COLOR)) {
                mKeyFlashCustomizedColorResult = (CaptureResult.Key<int[]>) resultKey;
            } else if (resultKey.getName().equals(FLASH_CALIBRATION_STATE_KEY)) {
                mKeyFlashCalibrationState = (CaptureResult.Key<int[]>) resultKey;
            }
        }
        List<CaptureRequest.Key<?>> requestKeyList = cs.getAvailableCaptureRequestKeys();
        for (CaptureRequest.Key<?> requestKey : requestKeyList) {
            if (requestKey.getName().equals(HDR_KEY_DETECTION_MODE)) {
                mKeyHdrRequestMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(HDR_KEY_SESSION_MODE)) {
                mKeyHdrRequsetSessionMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(EIS_KEY_SESSION_PARAMETER)) {
                mKeyEisSessionParameter = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(FPS60_KEY_SESSION_PARAMETER)) {
                mKeyFps60SessionParameter = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(ZSL_KEY_MODE_REQUEST)) {
                mKeyZslMode = (CaptureRequest.Key<byte[]>) requestKey;
            } else if (requestKey.getName().equals(AIS_REQUEST_MODE_KEY_NAME)) {
                mKeyAisRequestMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(ISO_KEY_CONTROL_SPEED)) {
                mKeyIsoRequestMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(CS_KEY_CAPTURE_REQUEST)) {
                mKeyCshotRequestMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(P2_KEY_NOTIFICATION_TRIGGER)) {
                mKeyP2NotificationRequestMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(ASD_REQUEST_MODE_KEY_NAME)) {
                mKeyAsdRequestMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(THUMBNAIL_KEY_POSTVIEW_SIZE)) {
                mKeyPostViewRequestSizeMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(PREVIEW_EIS_PARAMETER)) {
                mKeyPreviewEisParameter = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(FLASH_CALIBRATION_REQUEST_KEY)) {
                mKeyFlashCalibrationRequest = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(BG_SERVICE_PRERELEASE)) {
                mKeyBGServicePrerelease = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(BG_SERVICE_IMAGEREADER_ID)) {
                mKeyBGServiceImagereaderId = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(SMVR_REQUEST_MODE)) {
                mKeySMVRRequestMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(SMVR_V2_REQUEST_MODE)) {
                mKeySMVRV2RequestMode = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(PLATFORM_CAMERA_KEY)) {
                mKeyPlatformCamera = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(MTK_MULTICAM_FEATURE_MULTI_CAM_AF_ROI)){
                mKeyMultiCamAfRoi = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(MTK_MULTICAM_FEATURE_MULTI_CAM_AE_ROI)){
                mKeyMultiCamAeRoi = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(TPI_REQUEST_KEY)){
                mKeyTpiRequestValue = (CaptureRequest.Key<int[]>) requestKey;
            } else if (requestKey.getName().equals(MTK_CAMERA_PREVIEW_COMPRESSION)){
                mKeyPreviewCompressionRequest = (CaptureRequest.Key<int[]>) requestKey;
            }else if (requestKey.getName().equals(ZMMANUALING_REQUEST_RATIO_KEY)){
                mKeyZoomManualingRequestRatio = (CaptureRequest.Key<int[]>) requestKey;
            }
        }
    }


    public ArrayList<Size> getAvailableThumbnailSizes() {
        return mKeyThumbnailSizes;
    }

    /**
     * Get hdr available photo mode keys.
     *
     * @return this key.
     */
    public CameraCharacteristics.Key<int[]> getKeyHdrAvailablePhotoModes() {
        return mKeyHdrAvailablePhotoModes;
    }

    /**
     * Get vsdof keys OpticalZoomSets.
     *
     * @return this key.
     */
    public CameraCharacteristics.Key<int[]> getKeyVsdofKeyOpticalZoomSets() {
        return mKeyVsdofKeyOpticalZoomSets;
    }

    /**
     * Get hdr available video mode keys.
     *
     * @return this key.
     */
    public CameraCharacteristics.Key<int[]> getKeyHdrAvailableVideoModes() {
        return mKeyHdrAvailableVideoModes;
    }

    /**
     * Get hdr detection result keys.
     * @return this key.
     */
    public CaptureResult.Key<int[]> getKeyHdrDetectionResult () {
        return mKeyHdrDetectionResult;
    }

    /**
     * Get hdr request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyHdrRequestMode () {
        return mKeyHdrRequestMode;
    }

    /**
     * Get hdr request session mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyHdrRequsetSessionMode () {
        return mKeyHdrRequsetSessionMode;
    }
    /**
     * Get Ainr session parameter key.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyAinrRequsetSessionMode() {
        return mKeyAinrRequsetSessionMode;
    }

    /**
     * Get Eis session parameter key.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyEisRequsetSessionParameter() {
        return mKeyEisSessionParameter;
    }

    /**
     * Get Fps60 session parameter key.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyFps60RequsetSessionParameter() {
        return mKeyFps60SessionParameter;
    }

    /**
     * Get preview Eis parameter key.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyPreviewEisParameter() {
        return mKeyPreviewEisParameter;
    }

    /**
     * Get AIS available mode keys.
     * @return this key.
     */
    public CameraCharacteristics.Key<int[]> getKeyAisAvailableModes() {
        return mKeyAisAvailableModes;
    }

    /**
     * Get AIS request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyAisRequestMode() {
        return mKeyAisRequestMode;
    }

    /**
     * Get AIS result keys.
     * @return this key.
     */
    public CaptureResult.Key<int[]> getKeyAisResult() {
        return mKeyAisResult;
    }

    /**
     * Get ISO request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyIsoRequestMode() {
        return mKeyIsoRequestMode;
    }

    /**
     * Whether Flash customized flow is supported or not.
     * @return True if flash customized flow supported.
     */
    public boolean isFlashCustomizedAvailable() {
        return mIsFlashCustomizedAvailable;
    }

    /**
     * Get flash customized result keys.
     * @return this key.
     */
    public CaptureResult.Key<byte[]> getKeyFlashCustomizedResult() {
        return mKeyFlashCustomizedResult;
    }

    /**
     * Get flash customized color result keys.
     *
     * @return this key.
     */
    public CaptureResult.Key<int[]> getKeyFlashCustomizedColorResult() {
        return mKeyFlashCustomizedColorResult;
    }

    /**
     * Judge postView thumbnail support or not.
     * @return True is support, false is not support
     */
    public Boolean isThumbnailPostViewSupport() {
        return mThumbnailPostViewSupport;
    }

    /**
     * Get post view size request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyPostViewRequestSizeMode() {
        return mKeyPostViewRequestSizeMode;
    }

    /**
     * Get plamtform Camera KEY
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyPlatformCamera() {
        return mKeyPlatformCamera;
    }
    public CaptureRequest.Key<int[]> getKeyMultiCamAfRoi(){
        return mKeyMultiCamAfRoi;
    }

    public CaptureRequest.Key<int[]> getKeyMultiCamAeRoi(){
        return mKeyMultiCamAeRoi;
    }
    /**
     * Judge CaptureRequest.CONTROL_ENABLE_ZSL support or not.
     * @return True is support, false is not support
     */
    public Boolean isZslSupport() {
        return mZslSupport;
    }


    /**
     * Get ZSL request mode key.
     * @return this key.
     */
    public CaptureRequest.Key<byte[]> getKeyZslRequestKey() {
        return mKeyZslMode;
    }

    /**
     * Judge CShot support or not.
     * @return True is support, false is not support
     */
    public Boolean isCshotSupport() {
        return mCshotSupport;
    }

    /**
     * Judge BG service support or not.
     * @return True is support, false is not support
     */
    public Boolean isBGServiceSupport() {
        return mBGServiceSupport;
    }

    /**
     * Judge ZM support or not.
     *
     * @return True is support, false is not support
     */
    public Boolean isZoomManualingSupport() {
        return mZoomManualingSupport;
    }
    /**
     * Judge stereo mode support or not.
     * @return True is support, false is not support
     */
    public Boolean isStereoModeSupport() {
        return mStereoModeSupport;
    }

    /**
     * Judge dual zoom support or not.
     * @return True is support, false is not support
     */
    public Boolean isDualZoomSupport() {
        return mDualZoomSupport;
    }

    /**
     * Judge focal length support or not.
     * @return True is support, false is not support
     */
    public Boolean isFocalLengthSupport() {
        return mFocalLengthSupport;
    }

    /**
     * Get Cshot request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyCshotRequestMode() {
        return mKeyCshotRequestMode;
    }

    /**
     * Judge speed up support or not.
     * @return True is support, false is not support
     */
    public Boolean isSpeedUpSupport() {
        return mSpeedUpSupported;
    }

    /**
     * Get P2Notification request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyP2NotificationRequestMode() {
        return mKeyP2NotificationRequestMode;
    }

    /**
     * Get P2Notification result keys.
     * @return this key.
     */
    public CaptureResult.Key<int[]> getKeyP2NotificationResult() {
        return mKeyP2NotificationResult;
    }

    /**
     * Get ASD available mode keys.
     * @return this key.
     */
    public CameraCharacteristics.Key<int[]> getKeyAsdAvailableModes() {
        return mKeyAsdAvailableModes;
    }

    /**
     * Get ASD request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyAsdRequestMode() {
        return mKeyAsdRequestMode;
    }

    /**
     * Get ASD result keys.
     * @return this key.
     */
    public CaptureResult.Key<int[]> getKeyAsdResult() {
        return mKeyAsdResult;
    }

    /**
     * Get whether flash calibration supported or not.
     *
     * @return True if supported flash calibration.
     */
    public boolean isFlashCalibrationSupported() {
        return mIsFlashCalibrationSupported;
    }

    /**
     * Get flash calibration request key.
     * @return Flash calibration key which used to enable or disable flash calibration.
     */
    public CaptureRequest.Key<int[]>  getKeyFlashCalibrationRequest() {
        return mKeyFlashCalibrationRequest;
    }

    /**
     * Get flash calibration state key.
     * @return Flash calibration key which used to explain flash calibration is processing or not.
     */
    public CaptureResult.Key<int[]>  getKeyFlashCalibrationState() {
        return mKeyFlashCalibrationState;
    }

    /**
     * Get flash calibartion result key.
     *
     * @return Flash calibration result key which used to indicate whether flash calibration is
     * success or not.
     */
    public CaptureResult.Key<int[]> getKeyFlashCalibrationResult() {
        return mKeyFlashCalibrationResult;
    }

     /**
     * Get background service pre-release keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyBGServicePrerelease() {
        return mKeyBGServicePrerelease;
    }

    /**
     * Get background service imagereader id keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyBGServiceImagereaderId() {
        return mKeyBGServiceImagereaderId;
    }

    /*
     * Get SMVR available mode keys.
     * @return this key.
     */
    public CameraCharacteristics.Key<int[]> getKeySMVRAvailableModes() {
        return mKeySMVRAvailableModes;
    }

    public CameraCharacteristics.Key<int[]> getKeySMVRV2AvailableModes() {
        return mKeySMVRV2AvailableModes;
    }


    /**
     * Get SMVR request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeySMVRRequestMode() {
        return mKeySMVRRequestMode;
    }

    /**
     * Get SMVR hfps request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeySMVRV2RequestMode() {
        return mKeySMVRV2RequestMode;
    }

    /**
     * Get SMVR burst result keys.
     * @return this key.
     */
    public CaptureResult.Key<int[]> getKeySMVRBurstResult() {
        return mKeySMVRBurstResult;
    }

    /**
     * Get SMVR hfps burst result keys.
     * @return this key.
     */
    public CaptureResult.Key<int[]> getKeySMVRV2BurstResult() {
        return mKeySMVRV2BurstResult;
    }

    /**
     * Get VSDOF request key.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyVsdof() {
        return mKeyVsdof;
    }

    /**
     * Get VSDOF ZoomSet request key.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyVsdofZoomSet() {
        return mKeyVsdofZoomSet;
    }

    /**
     * Get HDR10 request key.
     *
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyHDR10() {
        return mKeyHdr10;
    }

    /*
     * Get M-Stream HDR available mode keys.
     * @return this key.
     */
    public CameraCharacteristics.Key<int[]> getKeyMSHDRAvailableModes() {
        return mKeyMSHDRMode;
    }

    public CameraCharacteristics.Key<int[]> getKeyStaggerHDRAvailableModes() {
        return mKeyStaggerHDRMode;
    }

    public CameraCharacteristics.Key<int[]> getKeyAvaliableHfpsMaxResolutions() {
        return mKeyAvaliableHfpsMaxResolutions;
    }

    public CameraCharacteristics.Key<int[]> getKeyAvaliableHfpsEisMaxResolutions() {
        return mKeyAvaliableHfpsEisMaxResolutions;
    }

    /**
     * Judge heif format support or not for aosp flow.
     *
     * @return True is support, false is not support
     */
    public boolean isHeicInfoSupported() {
        return mHeicInfoSupported;
    }

    public boolean isTrackingAfSupported() {
        return mTrackingAfSupported;
    }
    public boolean isHDR10EisSupprot() {
        return mHDR10EisSupprot;
    }
    public boolean isHDR10VssSupprot() {
        return mHDR10VssSupprot;
    }

    public boolean isFps60SdofSupprot() {
        return mFps60SdofSupprot;
    }

    public boolean isAinrSupprot() {
        return mAinrSupprot;
    }

    public CameraCharacteristics.Key<int[]> getKeyTpiSupportValue() {
         return mKeyTpiSupportValue;
    }

    public CaptureRequest.Key<int[]> getKeyTpiRequestValue() {
        return mKeyTpiRequestValue;
    }

    /**
     * @return the request key for afbc
     */
    public CaptureRequest.Key<int[]> getKeyPreviewCompressionRequest() {
        return mKeyPreviewCompressionRequest;
    }

    /**
     * @return the supported compression modes key
     */
    public CameraCharacteristics.Key<int[]> getKeyPreviewCompressionSupportModes() {
        return mKeyPreviewCompressionSupportModes;
    }

    public CaptureRequest.Key<int[]> getKeyMultiCamConfigScalerCropRegion() {
        return mKeyMultiCamConfigScalerCropRegion;
    }
    /**
     * Get ZoomManualing request mode keys.
     * @return this key.
     */
    public CaptureRequest.Key<int[]> getKeyZoomManualingRequestMode() {
        return mKeyZoomManualingRequestRatio;
    }
}
