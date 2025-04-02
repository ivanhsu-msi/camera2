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
 *     MediaTek Inc. (C) 2020. All rights reserved.
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

package com.mediatek.camera.feature.setting.hdr10;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;
import com.mediatek.camera.portability.SystemProperties;

public class Hdr10Restriction {

    private static RelationGroup mOffHfpsRelationGroup = new RelationGroup();
    private static RelationGroup mOffHfpsLpRelationGroup = new RelationGroup();
    private static RelationGroup mSupHfpsRelationGroup = new RelationGroup();
    private static RelationGroup mSupHfpsLpRelationGroup = new RelationGroup();

    /**
     * Get Fps60 restriction.
     *
     * @return Restriction group.
     */
    static {
        mOffHfpsRelationGroup.setHeaderKey("key_hdr10");
        mOffHfpsRelationGroup.setBodyKeys("key_eis");
        mOffHfpsRelationGroup.replaceRelation(
                new Relation.Builder("key_hdr10",
                        "on")
                        .addBody("key_eis", "off", "off")
                        .build());
    }

    static {
        mOffHfpsLpRelationGroup.setHeaderKey("key_hdr10");
        mOffHfpsLpRelationGroup.setBodyKeys("key_eis");
        mOffHfpsLpRelationGroup.setBodyKeys("key_dual_zoom");
        mOffHfpsLpRelationGroup.replaceRelation(
                new Relation.Builder("key_hdr10",
                        "on")
                        .addBody("key_dual_zoom","off","off")
                        .build());
        mOffHfpsLpRelationGroup.addRelation(
                new Relation.Builder("key_hdr10",
                        "off")
                        .addBody("key_dual_zoom","on","on,off")
                        .build());
    }


    static {
        mSupHfpsRelationGroup.setHeaderKey("key_hdr10");
        mSupHfpsRelationGroup.setBodyKeys("key_eis");
        mSupHfpsRelationGroup.replaceRelation(
                new Relation.Builder("key_hdr10",
                        "on")
                        .addBody("key_eis", "off", "off")
                        .build());
    }

    static  {
        mSupHfpsLpRelationGroup.setHeaderKey("key_hdr10");
        mSupHfpsLpRelationGroup.setBodyKeys("key_eis");
        mSupHfpsLpRelationGroup.setBodyKeys("key_dual_zoom");
        mSupHfpsLpRelationGroup.replaceRelation(
                new Relation.Builder("key_hdr10",
                        "on")
                        .addBody("key_dual_zoom","off","off")
                        .build());
        mSupHfpsLpRelationGroup.addRelation(
                new Relation.Builder("key_hdr10",
                        "off")
                        .addBody("key_dual_zoom","on","on,off")
                        .build());
    }
    /**
     * Get Fps60 restriction.
     *
     * @return Restriction group.
     */
    static RelationGroup getOffHfpsRelationGroup(){
        return mOffHfpsRelationGroup;
    }
    static RelationGroup getOffHfpsLpRelationGroup(){
        return mOffHfpsLpRelationGroup;
    }
    static RelationGroup getSupHfpsRelationGroup(){
        return mSupHfpsRelationGroup;
    }
    static RelationGroup getSupHfpsLpRelationGroup(){
        return mSupHfpsLpRelationGroup;
    }
}
