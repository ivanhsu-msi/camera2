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
package com.mediatek.aovtestapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mediatek.aovtestapp.KeepAliveService;
import com.mediatek.aovtestapp.Log;
import com.mediatek.aovtestapp.MainApplication;
import com.mediatek.aovtestapp.R;
import com.mediatek.aovtestapp.SystemProperties;
import com.mediatek.aovtestapp.module.AovDetectionObjectCommon;
import com.mediatek.aovtestapp.presenter.IPresenter;
import com.mediatek.aovtestapp.presenter.Presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements IView, OnCheckedChangeListener, OnItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private IPresenter mPresenter;

    private Spinner mCameraIdSpinner;
    private Spinner mSizeSpinner;
    private Spinner mFpsSpinner;
    private Spinner mFDModeSpinner;
    private Switch mGazeSwitch;
    private Switch mQRCodeModesSwitch;
    private Switch mGestureSwitch;
    private Switch mDisplaySwitch;
    private Switch mModesSwitch;
    private Switch mApplySwitch;
    private TextView mSwitchTimeTextView;
    private TextView mFDResultTextView;
    private TextView mQrCodeResultTextView;
    private TextView mGestureResultTextView;
    private TextView mGazeResultTextView;
    private TextView mSwitchTimeNewTextView;
    private TextView mGazeDetectTextView;

    private int mCurrentCameraId;

    private YUVRenderView mGlSurfaceView;
    private boolean mResumeCalledDuringOnCreate;
    private TextView mMultiResultTextView;
    private TextView mFDNumsTextView;
    private MainApplication mMainApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate] +");
        super.onCreate(savedInstanceState);

        //avoid boot complete could not received
        boolean appCanbeShown = SystemProperties.getInt("persist.vendor.aovap.force.show", 0) == 1
                || SystemProperties.getInt("ro.vendor.mtk_aov_app_support", 0) == 1;
        if (!appCanbeShown) {
            Log.d(TAG, "[onCreate] app closed due to SystemProperty forbidden !");
            finish();
            Process.killProcess(Process.myPid());
        }
        if (mMainApplication == null) {
            mMainApplication = (MainApplication) getApplication();
        }

        setContentView(R.layout.activity_main);
        mPresenter = new Presenter(this);
        mPresenter.onCreate(this);
        mResumeCalledDuringOnCreate = true;

        Intent intent = new Intent(getApplicationContext(), KeepAliveService.class);
        startForegroundService(intent);

        Log.d(TAG, "[onCreate] -");
    }


    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.d(TAG, "[onContentChanged] view init started");
        mCameraIdSpinner = findViewById(R.id.sp_camera_id);
        mSizeSpinner = findViewById(R.id.sp_size);
        mFpsSpinner = findViewById(R.id.sp_fps);
        mFDModeSpinner = findViewById(R.id.sp_fd_modes);

        mGazeSwitch = findViewById(R.id.switch_gaze);
        mQRCodeModesSwitch = findViewById(R.id.switch_qr_code_modes);
        mGestureSwitch = findViewById(R.id.switch_gestures);
        mDisplaySwitch = findViewById(R.id.switch_display);
        mModesSwitch = findViewById(R.id.switch_modes);
        mApplySwitch = findViewById(R.id.switch_applied);

        mSwitchTimeTextView = findViewById(R.id.tv_switch_time);
        mFDResultTextView = findViewById(R.id.tv_fd_result);
        mQrCodeResultTextView = findViewById(R.id.tv_qr_code_result);
        mGestureResultTextView = findViewById(R.id.tv_gesture_result);
        mGazeResultTextView = findViewById(R.id.tv_gaze_result);
        mMultiResultTextView = findViewById(R.id.tv_multi_results);
        mFDNumsTextView = findViewById(R.id.id_fd_nums);
        mSwitchTimeNewTextView = findViewById(R.id.tv_switch_time_new);
        mGazeDetectTextView = findViewById(R.id.tv_gaze_detected);

        mGlSurfaceView = findViewById(R.id.iv_preview);


        mCameraIdSpinner.setOnItemSelectedListener(this);
        mApplySwitch.setOnCheckedChangeListener(this);
        mSizeSpinner.setOnItemSelectedListener(this);
        Log.d(TAG, "[onContentChanged] view init ended");

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedSpinnerText = parent.getItemAtPosition(position).toString();
        Log.d(TAG, "[onItemSelected] selectedSpinnerText " + selectedSpinnerText );
        switch (parent.getId()) {
            case R.id.sp_camera_id:
                int i = Integer.parseInt(selectedSpinnerText);
                if (mCurrentCameraId != i) {
                    mPresenter.onCameraIdChanged(mCurrentCameraId = i);
                }
                break;

            case R.id.sp_size:
                mPresenter.onResolutionChanged(selectedSpinnerText);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_applied:
                onApplyTurnStateChanged(isChecked);
                break;
            default:
                break;

        }

    }


    @Override
    public void setCameraIdList(final int[] idList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> dataList = new ArrayList<>(idList.length);
                for (int i = 0; i < idList.length; i++) {
                    dataList.add(idList[i] + "");
                }
                SimpleArrayAdapter adapter = new SimpleArrayAdapter<>(MainActivity.this, dataList);
                mCameraIdSpinner.setAdapter(adapter);
            }
        });

    }

    @Override
    public void startService(String cmd) {
        Intent intent = new Intent(getApplicationContext(), KeepAliveService.class);
        intent.putExtra("executeCmd", cmd);
        startForegroundService(intent);
    }

    @Override
    public void wakeUpScreen() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, MainActivity.class.getName());
            wl.acquire(10000);
            wl.release();
        }
//        // 屏幕解锁
//        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("AovAp_unlock");
//        // 屏幕锁定
//        keyguardLock.reenableKeyguard();
//        keyguardLock.disableKeyguard(); // 解锁

    }

    @Override
    public void showSwitchTime(final long switchTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwitchTimeNewTextView.setText(String.format(Locale.getDefault(),"SwitchTime : %d ms",switchTime));
            }
        });
    }

    @Override
    public void setGazeResultNew(final boolean isGazeDetected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGazeDetectTextView.setText(String.format(Locale.getDefault(),"GazeDetect : %s",String.valueOf(isGazeDetected)));
            }
        });
    }

    @Override
    public String getCurrentSelectCameraId() {
        return (String) mCameraIdSpinner.getSelectedItem();
    }

    @Override
    public void setPictureSizes(final String[] resolutionList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleArrayAdapter adapter = new SimpleArrayAdapter<>(MainActivity.this, resolutionList);
                mSizeSpinner.setAdapter(adapter);
            }
        });

    }

    @Override
    public String getCurrentSelectedResolution() {
        return (String) mSizeSpinner.getSelectedItem();
    }

    @Override
    public void setFps(final String[] ranges) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleArrayAdapter adapter = new SimpleArrayAdapter<>(MainActivity.this, ranges);
                mFpsSpinner.setAdapter(adapter);
            }
        });

    }

    @Override
    public Integer getCurrentSelectedFps() {
        String selectedItem = (String) mFpsSpinner.getSelectedItem();
        Log.i(TAG, "[getCurrentSelectedFps] selectedItem = " + selectedItem);
        return Integer.valueOf(selectedItem);
    }

    @Override
    public void setFDMode(final String[] fdMode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleArrayAdapter adapter = new SimpleArrayAdapter<>(MainActivity.this, fdMode);
                mFDModeSpinner.setAdapter(adapter);
            }
        });

    }

    @Override
    public String getCurrentSelectedFDMode() {
        return (String) mFDModeSpinner.getSelectedItem();
    }

    @Override
    public boolean isGazeTurnedOn() {
        return mGazeSwitch.isChecked();
    }

    @Override
    public boolean isQRCodeTurnedOn() {
        return mQRCodeModesSwitch.isChecked();
    }

    @Override
    public boolean isGestureTurnedOn() {
        return mGestureSwitch.isChecked();
    }

    @Override
    public boolean isDisplayTurnedOn() {
        return mDisplaySwitch.isChecked();
    }

    @Override
    public boolean isModeTurnedOn() {
        return mModesSwitch.isChecked();
    }

    @Override
    public void onApplyTurnStateChanged(boolean on) {
        //disable all other ui if current apply button is opened
        Log.i(TAG, "[onApplyTurnStateChanged] + on = " + on);
        enableVisibleChooserViewAndOtherViewToAct(!on);

        mPresenter.actApplyChanged(!on);
        Log.i(TAG, "[onApplyTurnStateChanged] -");
    }

    @Override
    public void enableVisibleChooserViewAndOtherViewToAct(final boolean env) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //current is environment need disable to choose
                mCameraIdSpinner.setEnabled(env);
                mSizeSpinner.setEnabled(env);
                mFpsSpinner.setEnabled(env);
                if (((RelativeLayout) mFDModeSpinner.getParent()).getVisibility() == View.VISIBLE) {
                    mFDModeSpinner.setEnabled(env);
                }
                if (((RelativeLayout) mGazeSwitch.getParent()).getVisibility() == View.VISIBLE) {
                    mGazeSwitch.setEnabled(env);
                }
                if (((RelativeLayout) mQRCodeModesSwitch.getParent()).getVisibility() == View.VISIBLE) {
                    mQRCodeModesSwitch.setEnabled(env);
                }
                if (((RelativeLayout) mGestureSwitch.getParent()).getVisibility() == View.VISIBLE) {
                    mGestureSwitch.setEnabled(env);
                }

                mDisplaySwitch.setEnabled(env);
                mModesSwitch.setEnabled(env);
            }
        });

    }

    @Override
    public void visibleAvailableModes(List<String> availableModes) {
        //refresh AvailableModes
        boolean enable = availableModes.contains(AovDetectionObjectCommon.eOBJECT_FACE_FULL + "")
                || availableModes.contains(AovDetectionObjectCommon.eOBJECT_FACE_SIMPLE + "");
        ((RelativeLayout) mFDModeSpinner.getParent()).setVisibility(enable ? View.VISIBLE : View.GONE);

        ((RelativeLayout) mGazeSwitch.getParent()).setVisibility(availableModes.contains(AovDetectionObjectCommon.eOBJECT_GAZE + "") ? View.VISIBLE : View.GONE);
        ((RelativeLayout) mGestureSwitch.getParent()).setVisibility(availableModes.contains(AovDetectionObjectCommon.eOBJECT_GESTURE + "") ? View.VISIBLE : View.GONE);
        ((RelativeLayout) mQRCodeModesSwitch.getParent()).setVisibility(availableModes.contains(AovDetectionObjectCommon.eOBJECT_QRCODE_SCANNER + "") ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setMultiResultExceptBuffer(final HashMap<String, Object> data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMultiResultTextView.setText(data.toString());
            }
        });

    }

    @Override
    public void setFDNums(final String fdNums) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFDNumsTextView.setText(fdNums);
            }
        });
    }

    @Override
    public void setSwitchTime(final int ms) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwitchTimeTextView.setText(String.format(Locale.getDefault(), "%d ms", ms));
            }
        });

    }

    @Override
    public void setFDResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFDResultTextView.setText(result + "");
            }
        });

    }

    @Override
    public void setQRCodeResult(final String qrCodeResult) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mQrCodeResultTextView.setText(qrCodeResult + "");
            }
        });

    }

    @Override
    public void setGestureResult(final String gestureResult) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGestureResultTextView.setText(gestureResult + "");
            }
        });
    }

    @Override
    public void setGazeResult(final String gazeResult) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGazeResultTextView.setText(gazeResult + "");
            }
        });

    }

    @Override
    public void drawCanvasFrameByFrame(final byte[] buffer,final int frameWidth,
                                       final int frameHeight,final int stride) {
        if (!isModeTurnedOn()){
            // for measure power ,mode turned off do not need draw buffer
            return;
        }

        boolean bufferChanged = mGlSurfaceView.setParams(true, frameWidth , frameHeight,stride);
        if (!bufferChanged){
            mGlSurfaceView.newDataArrived(buffer);
        }

    }

    @Override
    public void applyManually() {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //false means apply change request
                if (mApplySwitch.isChecked() != false) {
                    mApplySwitch.setChecked(false);
                } else {
                    onCheckedChanged(mApplySwitch, false);
                }
            }
        });*/

    }

    @Override
    public void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void quitApp() {
        Log.i(TAG, "[quitApp] +");
        MainActivity.this.finish();
        Log.i(TAG, "[quitApp] -");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "[onResume] +");
//        if (!mResumeCalledDuringOnCreate){
//            mPresenter.onResume();
//        }
        Log.d(TAG, "[onResume] -");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "[onPause] +");
        mPresenter.onPause();
        mResumeCalledDuringOnCreate = false;
        Log.d(TAG, "[onPause] -");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[onDestroy] +");
        mPresenter.onDestroy();
        Log.d(TAG, "[onDestroy] -");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //inherit from widget
    }
}
