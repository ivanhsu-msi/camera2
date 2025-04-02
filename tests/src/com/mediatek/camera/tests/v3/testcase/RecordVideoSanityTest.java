package com.mediatek.camera.tests.v3.testcase;


import android.util.Log;

import com.mediatek.camera.tests.v3.annotation.type.SanityTest;
import com.mediatek.camera.tests.v3.arch.MetaCase;
import com.mediatek.camera.tests.v3.checker.CameraFacingChecker;
import com.mediatek.camera.tests.v3.checker.PreviewChecker;
import com.mediatek.camera.tests.v3.checker.ThumbnailChecker;
import com.mediatek.camera.tests.v3.checker.ThumbnailShownInGalleryChecker;
import com.mediatek.camera.tests.v3.checker.VideoDurationChecker;
import com.mediatek.camera.tests.v3.observer.VideoSavedObserver;
import com.mediatek.camera.tests.v3.operator.BackToCameraOperator;
import com.mediatek.camera.tests.v3.operator.GoToGalleryOperator;
import com.mediatek.camera.tests.v3.operator.RecordVideoOperator;
import com.mediatek.camera.tests.v3.operator.SwitchCameraOperator;
import com.mediatek.camera.tests.v3.operator.SwitchPhotoVideoOperator;

import org.junit.Test;


public class RecordVideoSanityTest extends BaseCameraTestCase {
    private static final String TAG = "RecordVideoSanityTest";
    @Override
    public void setUp() {
        Log.i(TAG, "TC_030 version 2020_03_24");
        Log.i(TAG, "setUp begin");
        mNotClearImagesVideos = true;
        super.setUp();
        Log.i(TAG, "setUp end");
    }

    /**
     * test record video.
     * Step 1: take main sensor begin
     * Step 2: swipe to video mode
     * Step 3: click video button and record 15s
     * Step 4: check video number
     * Step 5: take sub sensor begin
     * Step 6: switch to sub sensor
     * Step 7: go to gallery
     */
    @Test
    @SanityTest
    public void testRecordVideo() {
        new MetaCase("TC_030")
                .addOperator(new SwitchCameraOperator())
                .addChecker(new CameraFacingChecker())
                .addChecker(new PreviewChecker())
                .addOperator(new SwitchPhotoVideoOperator(), SwitchPhotoVideoOperator.INDEX_VIDEO)
                //.addChecker(new PreviewChecker())
                .observeBegin(new VideoSavedObserver())
                .addOperator(new RecordVideoOperator().setDuration(15))
                .observeEnd()
                .addChecker(new VideoDurationChecker(), VideoDurationChecker.INDEX_NORMAL)
                .addChecker(new ThumbnailChecker(), ThumbnailChecker.INDEX_HAS_THUMB)
                .addOperator(new GoToGalleryOperator())
                .addChecker(new ThumbnailShownInGalleryChecker())
                .addOperator(new BackToCameraOperator())
                .addChecker(new PreviewChecker())
                .run();
    }

}
