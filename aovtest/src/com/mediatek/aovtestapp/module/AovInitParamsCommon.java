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
 *   MediaTek Inc. (C) 2022. All rights reserved.
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
package com.mediatek.aovtestapp.module;

import java.lang.reflect.Field;

//This class used for wrap different api's request data
public class AovInitParamsCommon {
    public int sensorId = 0;
    public int sensorWidth = 0;
    public int sensorHeight = 0;
    public int frameRate = 0;
    public int detectionMode = 0;
    public boolean debugDisableCallback;
    public boolean debugCallbackImage;

    public <T> T toAidlRequestParams(Class<T> clazz) {
        T obj = null;
        try {
            obj = clazz.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        try {
            Field sensorIdField = clazz.getDeclaredField("sensorId");
            sensorIdField.setAccessible(true);
            sensorIdField.set(obj, this.sensorId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Field frameWidthField = clazz.getDeclaredField("sensorWidth");
            frameWidthField.setAccessible(true);
            frameWidthField.set(obj, this.sensorWidth);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Field frameHeightField = clazz.getDeclaredField("sensorHeight");
            frameHeightField.setAccessible(true);
            frameHeightField.set(obj, this.sensorHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Field frameRateField = clazz.getDeclaredField("frameRate");
            frameRateField.setAccessible(true);
            frameRateField.set(obj, this.frameRate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Field detectionModeField = clazz.getDeclaredField("detectionMode");
            detectionModeField.setAccessible(true);
            detectionModeField.set(obj, this.detectionMode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Field debugDisableCallbackField = clazz.getDeclaredField("debugDisableCallback");
            debugDisableCallbackField.setAccessible(true);
            debugDisableCallbackField.set(obj, this.debugDisableCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Field debugCallbackImageField = clazz.getDeclaredField("debugCallbackImage");
            debugCallbackImageField.setAccessible(true);
            debugCallbackImageField.set(obj, this.debugCallbackImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public String toString() {
        return "AovInitParamsCommon{" +
                "sensorId=" + sensorId +
                ", sensorWidth=" + sensorWidth +
                ", sensorHeight=" + sensorHeight +
                ", frameRate=" + frameRate +
                ", detectionMode=" + detectionMode +
                ", debugDisableCallback=" + debugDisableCallback +
                ", debugCallbackImage=" + debugCallbackImage +
                '}';
    }

}
