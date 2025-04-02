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

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;

public class VsdofQualityRestriction {
    private static final String KEY_VSDOF_QUALITY = "key_vsdof_quality";
    private static String HDR10KEY = "key_hdr10";
    private static String KEY_VIDEO_QUALITY = "key_video_quality";
    private static String HFPSKEY = "key_fps60";

    private static RelationGroup relationGroup = new RelationGroup();
    private static RelationGroup offHFPSrelationGroup = new RelationGroup();
    private static RelationGroup onHFPSrelationGroup = new RelationGroup();
    static {
        relationGroup.setHeaderKey(KEY_VSDOF_QUALITY);
        relationGroup.setBodyKeys(HDR10KEY);
        relationGroup.setBodyKeys(KEY_VIDEO_QUALITY);
        relationGroup.replaceRelation(
                new Relation.Builder(KEY_VSDOF_QUALITY,
                        "3")
                        .addBody(HDR10KEY, "off", "off")
                        .addBody(KEY_VIDEO_QUALITY, "0", "0")
                        .build());
    }
    static RelationGroup getVsdofQualityRelationGroup(){
        return relationGroup;
    }
    static RelationGroup getOffHFPSrelationGroup(String value){
        offHFPSrelationGroup.setHeaderKey(KEY_VSDOF_QUALITY);
        offHFPSrelationGroup.setBodyKeys(HFPSKEY);
        offHFPSrelationGroup.addRelation(
                new Relation.Builder(KEY_VSDOF_QUALITY,
                        value)
                        .addBody(HFPSKEY, "off", "off")
                        .build());
        return offHFPSrelationGroup;
    }
    static RelationGroup getOnHFPSrelationGroup(String value){
        onHFPSrelationGroup.setHeaderKey(KEY_VSDOF_QUALITY);
        onHFPSrelationGroup.setBodyKeys(HFPSKEY);
        onHFPSrelationGroup.addRelation(
                new Relation.Builder(KEY_VSDOF_QUALITY,
                        value)
                        .build());
        return onHFPSrelationGroup;
    }

}