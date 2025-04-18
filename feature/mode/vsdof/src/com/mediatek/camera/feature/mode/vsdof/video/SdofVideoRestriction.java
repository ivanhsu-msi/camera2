/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.feature.mode.vsdof.video;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;
import com.mediatek.camera.portability.SystemProperties;


/**
 * video mode restriction.
 */

public class SdofVideoRestriction {
    private static final String SDOF_VIDEO_MODE_KEY = SdofVideoMode.class.getName();
    private static RelationGroup sPreviewRelationGroup = new RelationGroup();
    private static RelationGroup sRecordingRelationGroupForMode = new RelationGroup();
    private static final String  VALUE_HDR10_STANDDAED = "1";

    static {
        sPreviewRelationGroup.setHeaderKey(SDOF_VIDEO_MODE_KEY);
        if (SystemProperties.getInt("vendor.mtk.camera.app.fd.video", 0) == 0) {
            sPreviewRelationGroup.setBodyKeys("key_scene_mode,key_face_detection," +
                    "key_video_quality,key_camera_switcher,key_dual_zoom,key_camera_zoom," +
                    "key_hdr10,key_standard_hdr10,key_anti_flicker,key_white_balance,key_flash,key_ainr");
            sPreviewRelationGroup.addRelation(
                    new Relation.Builder(SDOF_VIDEO_MODE_KEY, "preview")
                            .addBody("key_scene_mode", "off", "off")
                            .addBody("key_slow_motion_quality", "0", "0")
                            .addBody("key_face_detection", "off", "off")
                            //quality value will change in slowmotionmode.java according current
                            // value.
                            .addBody("key_video_quality", "109",
                                    "109")
                            .addBody("key_camera_switcher", "back", "back")
                            .addBody("key_dual_zoom", "off", "off")
                            .addBody("key_ainr", "off", "off")
                            .addBody("key_hdr10", "off", "off")
                            .addBody("key_standard_hdr10", VALUE_HDR10_STANDDAED, VALUE_HDR10_STANDDAED)
                            .addBody("key_anti_flicker", "off", "off")
                            .addBody("key_white_balance", "auto", "auto")
                            .addBody("key_flash","off","off")
                            .build());
        } else {
            sPreviewRelationGroup.setBodyKeys("key_scene_mode,key_video_quality," +
                    "key_camera_switcher,key_dual_zoom,key_camera_zoom,key_hdr10," +
                    "key_anti_flicker,key_standard_hdr10,key_white_balance,key_flash,key_ainr");
            sPreviewRelationGroup.addRelation(
                    new Relation.Builder(SDOF_VIDEO_MODE_KEY, "preview")
                            .addBody("key_scene_mode", "off", "off")
                            .addBody("key_slow_motion_quality", "0", "0")
                            //quality value will change in SdofVideoMode.java according current
                            // value.
                            .addBody("key_video_quality", "109", "109")
                            .addBody("key_camera_switcher", "back", "back")
                            .addBody("key_dual_zoom", "off", "off")
                            .addBody("key_hdr10", "off", "off")
                            .addBody("key_standard_hdr10", VALUE_HDR10_STANDDAED, VALUE_HDR10_STANDDAED)
                            .addBody("key_anti_flicker", "off", "off")
                            .addBody("key_white_balance", "auto", "auto")
                            .addBody("key_flash","off","off")
                            .addBody("key_ainr", "off", "off")
                            .build());
        }
    }

    /**
     * Video restriction witch are have setting ui.
     *
     * @return restriction list.
     */
    static RelationGroup getPreviewRelation() {
        return sPreviewRelationGroup;
    }

    /**
     * Video restriction witch are during recording.
     *
     * @return restriction list.
     */
    static RelationGroup getRecordingRelationForMode() {
        return sRecordingRelationGroupForMode;
    }

    /**
     * Video restriction for scene mode.
     *
     * @return scene mode restriction
     */
    static String getVideoSceneRestriction() {
        return "off," +
                "night," +
                "sunset," +
                "party," +
                "portrait," +
                "landscape," +
                "night-portrait," +
                "theatre," +
                "beach," +
                "snow," +
                "steadyphoto," +
                "sports," +
                "candlelight";
    }
}
