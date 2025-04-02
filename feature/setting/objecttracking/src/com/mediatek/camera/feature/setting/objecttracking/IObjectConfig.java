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

package com.mediatek.camera.feature.setting.objecttracking;

import com.mediatek.camera.common.utils.Size;

import java.util.List;

/**
 * It is the common flow to control object performing.
 */

public interface IObjectConfig {

    public static final String KEY_OBJECT_TRACKING = "key_object_tracking";
    public static final String OBJECT_TRACKING_ON = "on";
    public static final String OBJECT_TRACKING_OFF = "off";

    /**
     * Set object monitor to device to get the object flow state.
     *
     * @param monitor the monitor.
     */
    public void setObjectMonitor(ObjectDeviceCtrl.IObjectPerformerMonitor monitor);

    /**
     * this is to update image orientation to object algo.
     */
    public void updateImageOrientation();

    /**
     * Set device preview state.
     */
    public void resetObjectTrackingState();

    /**
     * For object tracking callback update flow.
     *
     * @param listener the object update listener.
     */
    public void setObjectTrackingUpdateListener(OnTrackedObjectUpdateListener listener);

    /**
     * listener object tracking device change.
     */
    public interface OnTrackedObjectUpdateListener {
        /**
         * The callback for object update.
         *
         * @param object the tracked object.
         */
        public void onTrackedObjectUpdate(Object object);

        /**
         * The callback for no object.
         */
        public void onTrackNoObject();
    }

    /**
     * For object tracking value update flow.
     *
     * @param listener the object value update listener.
     */
    public void setObjectValueUpdateListener(OnObjectValueUpdateListener listener);

    /**
     * listener object tracking setting values change.
     */
    public interface OnObjectValueUpdateListener {
        /**
         * The callback for object setting value.
         *
         * @param isSupport   the object tracking supported or not.
         * @param supportList the supported value for setting.
         */
        public void onObjectSettingValueUpdate(boolean isSupport, List<String> supportList);

        /**
         * The callback for preview size update.
         *
         * @return the preview size.
         */
        public Size onObjectPreviewSizeUpdate();

        /**
         * This is used to update image orientation for object algo.
         *
         * @return the image orientation.
         */
        public int onUpdateImageOrientation();

        public String onGetValue();
    }


}
