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

package com.mediatek.camera.feature.setting.eis;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;

/**
 * AIS restriction.
 */

public class EisRestriction {
    private static RelationGroup mOffHfpsRelationGroup = new RelationGroup();
    private static RelationGroup mOffHfpsLPRelationGroup = new RelationGroup();
    private static RelationGroup mSupHfpsRelationGroup = new RelationGroup();
    private static RelationGroup mSupHfpsLpRelationGroup = new RelationGroup();
    private static RelationGroup mCommonEisOnRestriction = new RelationGroup();
    private static final String KEY_HDR10 = "key_hdr10";

    /**
     *
     * @return Restriction group.
     */
    static {
        mOffHfpsRelationGroup.setHeaderKey("key_eis");
        mOffHfpsRelationGroup.setBodyKeys("key_fps60");
        mOffHfpsRelationGroup.setBodyKeys(KEY_HDR10);
        mOffHfpsRelationGroup.replaceRelation(
                new Relation.Builder("key_eis",
                        "on")
                        .addBody("key_fps60", "off", "off")
                        .addBody(KEY_HDR10,"off","off")
                        .build());
    }

    static {
        mOffHfpsLPRelationGroup.setHeaderKey("key_eis");
        mOffHfpsLPRelationGroup.setBodyKeys("key_fps60");
        mOffHfpsLPRelationGroup.replaceRelation(
                new Relation.Builder("key_eis",
                        "on")
                        .addBody("key_fps60", "off", "off")
                        .build());
    }

    /**
     * eis on ---> show 60fps setting and dismiss (hdr10)
     */
    static {
        mSupHfpsRelationGroup.setHeaderKey("key_eis");
        mSupHfpsRelationGroup.setBodyKeys("key_fps60");
        mSupHfpsRelationGroup.setBodyKeys(KEY_HDR10);
        mSupHfpsRelationGroup.replaceRelation(
                new Relation.Builder("key_eis",
                        "on")
                        .addBody(KEY_HDR10,"off","off")
                        .build());
    }

    static {
        mSupHfpsLpRelationGroup.setHeaderKey("key_eis");
        mSupHfpsLpRelationGroup.setBodyKeys("key_fps60");
        mSupHfpsLpRelationGroup.replaceRelation(
                new Relation.Builder("key_eis",
                        "on")
                        .build());
    }
    /**
     * eis on ----> turn off demo eis
     */
    static {
        mCommonEisOnRestriction.setHeaderKey("key_eis");
        mCommonEisOnRestriction.setBodyKeys(KEY_HDR10);

        mCommonEisOnRestriction.addRelation(
                new Relation.Builder("key_eis","on")
                        .addBody(KEY_HDR10,"off","off")
                        .build());

    }

    static RelationGroup getOffHfpsRelationGroup(){
        return mOffHfpsRelationGroup;
    }
    static RelationGroup getOffHfpsLpRelationGroup(){
        return mOffHfpsLPRelationGroup;
    }
    static RelationGroup getSupHfpsRelationGroup(){
        return mSupHfpsRelationGroup;
    }
    static RelationGroup getSupHfpsLpRelationGroup(){
        return mSupHfpsLpRelationGroup;
    }
    static RelationGroup getCommonEisRelation() {
        return mCommonEisOnRestriction;
    }

}
