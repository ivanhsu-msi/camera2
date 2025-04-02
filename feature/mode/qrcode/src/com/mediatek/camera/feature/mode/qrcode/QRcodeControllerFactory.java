package com.mediatek.camera.feature.mode.qrcode;

import android.app.Activity;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory;

import javax.annotation.Nonnull;

/**
 * Created by vend_wsd_am_029 on 2019/5/28.
 */

public class QRcodeControllerFactory {

    /**
     * Create the instance of {@link IQRcodeDeviceController} by API type.
     *
     * @param activity  the camera activity.
     * @param cameraApi the type of IDeviceController.
     * @param context   the camera context.
     * @return an instance of IDeviceController.
     */
    @Nonnull
    public IQRcodeDeviceController createDeviceController(
            @Nonnull Activity activity,
            @Nonnull CameraDeviceManagerFactory.CameraApi cameraApi,
            @Nonnull ICameraContext context) {
        return new QRcodeDevice2Controller(activity, context);
    }
}
