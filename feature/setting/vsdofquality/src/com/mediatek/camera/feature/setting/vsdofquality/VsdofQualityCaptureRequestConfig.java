/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2016. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.feature.setting.vsdofquality;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.loader.DeviceDescription;
import com.mediatek.camera.common.mode.CameraApiHelper;
import com.mediatek.camera.common.setting.ICameraSetting;
import com.mediatek.camera.common.setting.ISettingManager.SettingDevice2Requester;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.portability.CamcorderProfileEx;

import java.util.ArrayList;
import java.util.List;


/**
 * Configure vsdof quality in capture request in camera api2.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VsdofQualityCaptureRequestConfig implements ICameraSetting.ICaptureRequestConfigure {
    private static final LogUtil.Tag TAG =
                      new LogUtil.Tag(VsdofQualityCaptureRequestConfig.class.getSimpleName());
    private static final int QUALITY_INDEX_NUMBER = 2;
    private static final int QUALITY_MAX_NUMBER = 4;
    private CameraCharacteristics mCameraCharacteristics;
    private SettingDevice2Requester mDevice2Requester;
    private VsdofQuality mVsdofQuality;
    private List<Size> mSupportedSizes;
    private Context mContext;
    private static final String VSDOF_FEATURE_SUPPORTE_DVIDEOSIZES
            = "com.mediatek.vsdoffeature.vsdofFeatureSupportedVideoSizes";

    /**
     * vsdof quality capture request configure constructor.
     * @param quality The instance of {@link VsdofQuality}.
     * @param device2Requester The implementer of {@link SettingDevice2Requester}.
     */
    public VsdofQualityCaptureRequestConfig(VsdofQuality quality,
                                            SettingDevice2Requester device2Requester,
                                            Context context) {
        mVsdofQuality = quality;
        mDevice2Requester = device2Requester;
        mContext = context;
    }

    @Override
    public void setCameraCharacteristics(CameraCharacteristics characteristics) {
        mCameraCharacteristics = characteristics;
        mSupportedSizes = getSupportedVideoSizes();
        updateSupportedValues();
        mVsdofQuality.updateValue(getDefaultQuality());
        mVsdofQuality.onValueInitialized();
    }

    @Override
    public void configCaptureRequest(CaptureRequest.Builder captureBuilder) {

    }
  
    @Override
    public void configSessionParams(CaptureRequest.Builder captureBuilder) {

    }

    @Override
    public void configSessionSurface(List<Surface> surfaces) {

    }

    @Override
    public Surface configRawSurface() {
        return null;
    }

    @Override
    public CameraCaptureSession.CaptureCallback getRepeatingCaptureCallback() {
        return null;
    }
    /**
     * Get the max size as default value of vsdof quality.
     * @return getDefaultValue.
     */
    private String getDefaultQuality() {
        int defaultIndex = 0;
        if (mVsdofQuality.getSupportedPlatformValues().size() > QUALITY_INDEX_NUMBER) {
            defaultIndex = 1;
        }
        String defaultSize = mVsdofQuality.getSupportedPlatformValues().get(defaultIndex);
        return defaultSize;
    }

    private void updateSupportedValues() {
        List<String> supported = getSupportedListQuality(
                Integer.parseInt(mVsdofQuality.getCameraId()));
        mVsdofQuality.setSupportedPlatformValues(supported);
        mVsdofQuality.setEntryValues(supported);
        mVsdofQuality.setSupportedEntryValues(supported);
    }

    private List<String> getSupportedListQuality(int cameraId) {
        ArrayList<String> supported = new ArrayList<String>();
        generateSupportedList(cameraId, supported, VsdofQualityHelper.sMtkVsdofQualities);
        if (supported.isEmpty()) {
            generateSupportedList(cameraId, supported, VsdofQualityHelper.sVsdofQualities);
        }
        return supported;
    }

    private void generateSupportedList(int cameraId,
                                       ArrayList<String> supported, int[] defMatrix) {
        for (int i = 0; i < defMatrix.length && supported.size() < QUALITY_MAX_NUMBER; i++) {
            if (CamcorderProfile.hasProfile(cameraId, defMatrix[i])
                    && featureCharacteristics(cameraId, defMatrix[i])) {
                supported.add(Integer.toString(defMatrix[i]));
                LogHelper.d(TAG, "generateSupportedList add " + defMatrix[i]);
            }
        }
    }

    private boolean featureCharacteristics(int cameraId, int quality) {
        if(mSupportedSizes == null){
            return false;
        }
        CamcorderProfile profile = CamcorderProfileEx.getProfile(cameraId, quality);
        Size videoSz = new Size(profile.videoFrameWidth, profile.videoFrameHeight);
        boolean support = false;
        if (mSupportedSizes.contains(videoSz)) {
            support = true;
        }
        return support;
    }

    private List<Size> getSupportedVideoSizes() {
        try {
            int[] vsdofVideoValue =
                    CameraUtil.getStaticKeyResult(mCameraCharacteristics, VSDOF_FEATURE_SUPPORTE_DVIDEOSIZES);

            Size[] rawSizes = getSupportedSizeForClass();
            List<Size> sizes = new ArrayList<Size>();

            if (vsdofVideoValue == null || vsdofVideoValue.length == 0) {
                LogHelper.e(TAG, "[getSupportedVideoSizes]VSDOF_FEATURE_SUPPORTE_DVIDEOSIZES is null:");
                for (Size sz: rawSizes) {
                    sizes.add(sz);
                    LogHelper.e(TAG, "[getSupportedVideoSizes]Width:"+sz.getWidth()+",Height:"+sz.getHeight());
                }
            }else{
                for (Size sz: rawSizes) {
                    for (int i = 0; i < vsdofVideoValue.length; i = i + 2) {
                        if (sz.getWidth() == vsdofVideoValue[i] && (i+1) < vsdofVideoValue.length
                                && sz.getHeight() == vsdofVideoValue[i+1]){
                            sizes.add(sz);
                            LogHelper.e(TAG, "[getSupportedVideoSizes]Width:"+sz.getWidth()+",Height:"+sz.getHeight());
                        }
                    }
                }
            }
            return sizes;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Size[] getSupportedSizeForClass() throws CameraAccessException {
        if (mCameraCharacteristics == null) {
            LogHelper.e(TAG, "Can't get camera characteristics!");
            return null;
        }
        StreamConfigurationMap configMap =
                mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] availableSizes = configMap.getOutputSizes(android.media.MediaRecorder.class);
        Size[] highResAvailableSizes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            highResAvailableSizes = configMap.getHighResolutionOutputSizes(ImageFormat.PRIVATE);
        }
        if (highResAvailableSizes != null && highResAvailableSizes.length > 0) {
            Size[] allSizes = new Size[availableSizes.length + highResAvailableSizes.length];
            System.arraycopy(availableSizes, 0, allSizes, 0,
                    availableSizes.length);
            System.arraycopy(highResAvailableSizes, 0, allSizes, availableSizes.length,
                    highResAvailableSizes.length);
            availableSizes = allSizes;
        }
        return availableSizes;
    }

    public int[] getEISMaxSizeFps() {
        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mContext)
                .getDeviceDescriptionMap().get(String.valueOf(mVsdofQuality.getCameraId()));
        if (deviceDescription != null && 
            	deviceDescription.getKeyAvaliableHfpsEisMaxResolutions() != null) {
            CameraCharacteristics.Key<int[]> mKeyAvaliableHfpsEisMaxResolutions =
                    deviceDescription.getKeyAvaliableHfpsEisMaxResolutions();
            LogHelper.d(TAG, "getMaxSizeFps = " + mKeyAvaliableHfpsEisMaxResolutions);
            int[] keys = mCameraCharacteristics.get(mKeyAvaliableHfpsEisMaxResolutions);
            if (keys == null) {
                return null;
            }
            if (keys.length > 0) {
                int width = keys[0];
                int height = keys[1];
                int fps = keys[2];
                LogHelper.d(TAG, "getEISMaxSizeFps width =  " + width
                        + " | height = "+ height
                        + " |fps = " + fps);
                return new int[]{ width, height, fps};
            }
        }
        return null;
    }
    private Boolean compareSize(int[] currentValue, int[] maxValue) {
        float currentSize = currentValue[0] * currentValue[1];
        float maxSize = maxValue[0] * maxValue[1];
        return maxSize >= currentSize;
    }
    private boolean is60FPS() {
        if(mVsdofQuality.getSettingController().queryValue("key_fps60") == null){
            return false;
        }
        String currentValue = String.valueOf(
                mVsdofQuality.getSettingController().queryValue("key_fps60"));
        if ("on".equals(currentValue)) {
            return true;
        } else {
            return false;
        }
    }
    private int [] currentVideoSize(String currentValue) {
        LogHelper.d(TAG, "currentVideoSize = " + currentValue);
        CamcorderProfile cm = CamcorderProfile.get(Integer.parseInt(currentValue));
        LogHelper.d(TAG, "currentVideoSize width =  " + cm.videoFrameWidth
                + " | height = "+ cm.videoFrameHeight);
        return new int[]{cm.videoFrameWidth ,cm.videoFrameHeight };
    }

    boolean isSupportEIS(String value){
        if(!is60FPS()){
            return true;
        } else if (getEISMaxSizeFps() == null) {
            LogHelper.d(TAG, "is EIS SupportQuality no limit");
            return true;
        } else if (compareSize(currentVideoSize(value), getEISMaxSizeFps())) {
            LogHelper.d(TAG, "is EIS SupportQuality");
            return true;
        }else {
            LogHelper.d(TAG, "is EIS not SupportQuality");
            return false;
        }
    }

    boolean isFPS60Support(String value) {
        int[] keys = getFPSMaxSize();
        if (keys == null) {
            return false;
        }
        LogHelper.d(TAG, "getfps60MaxSizeFps width =  " + keys[0]
                + " | height = " + keys[1]);
        if (keys[0] * keys[1] >= currentVideoSize(value)[0] * currentVideoSize(value)[1]) {
            LogHelper.d(TAG, "is 60 fps SupportQuality");
            return true;
        }
        return false;
    }

    public int[] getFPSMaxSize() {
        DeviceDescription deviceDescription = CameraApiHelper.getDeviceSpec(mContext)
                .getDeviceDescriptionMap().get(String.valueOf(mVsdofQuality.getCameraId()));
        if (deviceDescription != null) {
            CameraCharacteristics.Key<int[]> mKeyAvaliableHfpsMaxResolutions =
                    deviceDescription.getKeyAvaliableHfpsMaxResolutions();
            if(mKeyAvaliableHfpsMaxResolutions !=null) {
                LogHelper.d(TAG, "mKeyAvaliableHfpsMaxResolutions = "
                        + mKeyAvaliableHfpsMaxResolutions);
                int[] keys = mCameraCharacteristics.get(mKeyAvaliableHfpsMaxResolutions);
                if (keys == null) {
                    return null;
                }
                LogHelper.d(TAG, "getfps60MaxSizeFps width =  " + keys[0]
                        + " | height = " + keys[1]);
                return keys;
            } else {
                LogHelper.d(TAG, "mKeyAvaliableHfpsMaxResolutions = null");
            }
        }
        return null;
    }

    /**
     * Send request when setting value is changed.
     */
    @Override
    public void sendSettingChangeRequest() {
        mDevice2Requester.requestRestartSession();
    }
}
