package com.mediatek.camera.feature.setting.videoformat;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;

public class VideoFormatRestriction {

    private static RelationGroup sRelation = new RelationGroup();
    private static RelationGroup multiRelation = new RelationGroup();

    static {
        sRelation.setHeaderKey("key_video_format");
        sRelation.setBodyKeys("key_hdr10");
        sRelation.addRelation(
                new Relation.Builder("key_video_format", "h264")
                        .addBody("key_hdr10", "off", "on,off")
                        .build());
    }


    static {
        //This relation is used for disable fps60 when choose HEVC ,because from hal's request
        //Some chip do not support "'fps60 + 4k' and h264"
        //another property "vendor.mtk.camera.app.4k60.off.h264.enable" is used disable this
        //restriction for qa test
        multiRelation.setHeaderKey("key_video_format");
        multiRelation.setBodyKeys("key_fps60");
        multiRelation.addRelation(
                new Relation.Builder("key_video_format", "h264")
                        .addBody("key_fps60", "off", "on,off")
                        .build());
    }

    /**
     * Restriction.
     *
     * @return restriction list.
     */
    static RelationGroup getRestriction() {
        return sRelation;
    }

    public static RelationGroup getMultiRelation() {
        return multiRelation;
    }
}
