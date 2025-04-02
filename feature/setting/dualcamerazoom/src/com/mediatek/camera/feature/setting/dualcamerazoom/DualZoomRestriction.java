package com.mediatek.camera.feature.setting.dualcamerazoom;

import com.mediatek.camera.common.relation.Relation;
import com.mediatek.camera.common.relation.RelationGroup;
import com.mediatek.camera.feature.setting.zoom.IZoomConfig;

public class DualZoomRestriction  {

	private static final String DUAL_ZOOM_KEY = IDualZoomConfig.KEY_DUAL_ZOOM;
	private static final String ZOOM_KEY = IZoomConfig.KEY_CAMERA_ZOOM;

	private static RelationGroup sRelation = new RelationGroup();
	private static RelationGroup sNoEISRelation = new RelationGroup();
	private static String EIS_KEY = "key_eis";

	/**
	 * Restriction witch are have setting ui.
	 *
	 * @return restriction list.
	 */
	static RelationGroup getRestriction() {
		return sRelation;
	}

	static RelationGroup getNoEISRelation() {
		return sNoEISRelation;
	}

	static {
		sRelation.setHeaderKey(DUAL_ZOOM_KEY);
		sRelation.setBodyKeys(ZOOM_KEY);
		sRelation.addRelation(
				new Relation.Builder(DUAL_ZOOM_KEY, "off")
						.addBody(ZOOM_KEY, "on", "on,off")
						.build());
		sRelation.addRelation(
				new Relation.Builder(DUAL_ZOOM_KEY, "on")
						.addBody(ZOOM_KEY, "off", "on,off")
						.build());
	}
	static {
		sNoEISRelation.setHeaderKey(DUAL_ZOOM_KEY);
		sNoEISRelation.setBodyKeys(ZOOM_KEY);
		sNoEISRelation.setBodyKeys(EIS_KEY);
		sNoEISRelation.addRelation(
				new Relation.Builder(DUAL_ZOOM_KEY, "off")
						.addBody(ZOOM_KEY, "on", "on,off")
						.build());
		sNoEISRelation.addRelation(
				new Relation.Builder(DUAL_ZOOM_KEY, "on")
						.addBody(EIS_KEY, "off", "off")
						.addBody(ZOOM_KEY, "off", "on,off")
						.build());
	}

}
