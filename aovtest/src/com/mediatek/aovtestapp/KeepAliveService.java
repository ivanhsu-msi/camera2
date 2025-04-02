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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.mediatek.aovtestapp.module.*;
import static androidx.core.app.NotificationCompat.PRIORITY_MIN;


//This service is a proxy to run Module on background
public class KeepAliveService extends Service implements Handler.Callback {
    private static final String  TAG = "KeepAliveService";
    private static final int RUNNING_CHECK = 0x123;
    private Handler mMainHandler;
    private int RUNNING_CHECK_DURATION = 5 * 1000;
    private ModuleAidl mModule;
    private boolean serviceConnected;
    private int NOTICE_ID = 100;


    @Override
    public void onCreate() {
        super.onCreate();
        mModule = ModuleAidl.getInstance();
        mMainHandler = new Handler(getMainLooper(),this);
        mMainHandler.sendEmptyMessageDelayed(RUNNING_CHECK, RUNNING_CHECK_DURATION);

        startForeground();


    }

    /* @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"[ onStartCommand ] +");
        if (mModule == null){
            return START_STICKY;
        }

        String executeCmd = intent.getStringExtra("executeCmd");
        if ("connect".equals(executeCmd) && !serviceConnected){
            serviceConnected = mModule.connect();
        }else if ("disconnect".equals(executeCmd) && serviceConnected){
            mModule.disconnect();
            serviceConnected = false;
        }
        Log.i(TAG,"[ onStartCommand ] - executeCmd = " + executeCmd);
        return START_STICKY;
    }*/

   @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"[onDestroy] + ");
        super.onDestroy();
        mMainHandler.removeMessages(RUNNING_CHECK);
        if (mModule != null){
//            mModule.stop();
//            mModule.disconnect();
        }
        Log.d(TAG,"[onDestroy] - ");
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case RUNNING_CHECK:
                Log.i(TAG,"service is running.");
                mMainHandler.sendEmptyMessageDelayed(RUNNING_CHECK, RUNNING_CHECK_DURATION);
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * 启动前台服务
     */
    private void startForeground() {
        String channelId = null;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("aovap", "ForegroundService");
        } else {
            channelId = "";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(NOTICE_ID, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }
}
