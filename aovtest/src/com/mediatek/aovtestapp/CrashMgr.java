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
package com.mediatek.aovtestapp;

import android.app.Activity;
import android.os.Looper;
import android.os.Process;

import java.lang.ref.WeakReference;
import java.util.List;

public class CrashMgr implements Thread.UncaughtExceptionHandler {

    private static final String  TAG = "CrashMgr";

    private static volatile CrashMgr INSTANCE;

    private List<WeakReference<Activity>> mWeakRefActivities;

    public void init(List<WeakReference<Activity>> activities) {
        Thread.setDefaultUncaughtExceptionHandler(this);
        mWeakRefActivities = activities;
        while (true) {
            try {
                Looper.loop();
            } catch (Throwable e) {
                e.printStackTrace();
                Log.d(TAG, "loop ui thread JE occurred ");
                removeAllActivities(activities);
            }
        }
    }

    private void removeAllActivities(List<WeakReference<Activity>> activities) {
        Log.i(TAG, "[removeAllActivities] activity removed due to JE ,activities size "
                + activities.size());
        for (WeakReference<Activity> activityRef : activities) {
            Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()){
                Log.i(TAG, "[removeAllActivities] activity " + activity);
                activity.finish();
            }
        }
        activities.clear();
        Process.killProcess(Process.myPid());
    }

    public static CrashMgr getInstance() {
        if (INSTANCE == null) {
            synchronized (CrashMgr.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CrashMgr();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        removeAllActivities(mWeakRefActivities);
        Log.d(TAG, "uncaughtException none ui thread JE occurred ");
    }
}
