package com.mediatek.camera.tests.v3.operator;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.tests.v3.arch.OperatorOne;
import com.mediatek.camera.tests.v3.arch.Page;
import com.mediatek.camera.tests.v3.arch.TestContext;
import com.mediatek.camera.tests.v3.util.LogHelper;
import com.mediatek.camera.tests.v3.util.ReflectUtils;
import com.mediatek.camera.tests.v3.util.Utils;

public class InitCameraParameterOperator extends OperatorOne {
    private static final LogUtil.Tag TAG = Utils.getTestTag(InitCameraParameterOperator.class
            .getSimpleName());

    private static final String BACK_CAMERA = "0";
    private static final String FRONT_CAMERA = "1";


    @Override
    protected void doOperate() {
        initWhenAPI2();
    }

    @Override
    public Page getPageBeforeOperate() {
        return null;
    }

    @Override
    public Page getPageAfterOperate() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Init camera parameters in both API1 and API2 for test environment needed";
    }

    private void initWhenAPI2() {

        CameraManager cameraManager = (CameraManager) ReflectUtils.createInstance(
                    ReflectUtils.getConstructor("android.hardware.camera2.CameraManager",
                            Context.class), Utils.getTargetContext());

        if (TestContext.mBackCameraCharacteristics == null) {
            try {
                LogHelper.d(TAG, "[initWhenAPI2] BACK_CAMERA");
                TestContext.mBackCameraCharacteristics =
                        cameraManager.getCameraCharacteristics(BACK_CAMERA);
            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[initWhenAPI2] back camera, CameraAccessException " + e);
            } catch (IllegalArgumentException e) {
                LogHelper.e(TAG, "[initWhenAPI2] back camera, IllegalArgumentException " + e);
            }
        }

        if (TestContext.mFrontCameraCharacteristics == null) {
            try {
                LogHelper.d(TAG, "[initWhenAPI2] FRONT_CAMERA");
                TestContext.mFrontCameraCharacteristics =
                        cameraManager.getCameraCharacteristics(FRONT_CAMERA);
            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[initWhenAPI2] front camera, CameraAccessException " + e);
            } catch (IllegalArgumentException e) {
                LogHelper.e(TAG, "[initWhenAPI2] front camera, IllegalArgumentException " + e);
            }
        }

        if (TestContext.mCameraNumber == 0) {
            try {
                String[] cameraIdList = cameraManager.getCameraIdList();
                if (cameraIdList == null || cameraIdList.length == 0) {
                    LogHelper.e(TAG, "[initWhenAPI2] Camera num is 0, Sensor should double check");
                    return;
                }
                for (String id : cameraIdList) {
                    LogHelper.d(TAG, "[initWhenAPI2] camera id is " + id);
                }
                TestContext.mCameraNumber = cameraIdList.length;

                LogHelper.d(TAG, "[initWhenAPI2] camera number is " + TestContext.mCameraNumber);

            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[initWhenAPI2] front camera, CameraAccessException " + e);
            }
        }
    }
}
