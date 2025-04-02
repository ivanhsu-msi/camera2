package com.mediatek.camera.tests.v3.util;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiWatcher;

import com.mediatek.camera.common.debug.LogUtil;

public class UnExceptedPopupUiWatcher implements UiWatcher {
    private static final LogUtil.Tag TAG = Utils.getTestTag(
            UnExceptedPopupUiWatcher.class.getSimpleName());
    private String[] arryButtonText={
            "ALLOW",
            "AGREE",
            "OK",
            "ok",
            "YES",
            "Y",
            "是",
            "确认",
            "确定",
            "否",
            "同意",
            "取消",
            "N",
            "Cancel"
    };

    @Override
    public boolean checkForCondition() {
        UiObject2 notResponding =
                Utils.getUiDevice().findObject(By.textContains("isn't responding"));
        UiObject2 closeApp = null;
        if (notResponding != null) {
            closeApp = Utils.getUiDevice().findObject(By.text("Close app"));
            if (closeApp != null) {
                closeApp.click();
            }
        }
        return clickDialog()  || (notResponding != null && closeApp != null);
    }

    private boolean clickDialog() {
        boolean hasbutton = false;
        for (int i = 0; i < arryButtonText.length; i++) {
            UiObject2 mClickButton = Utils.getUiDevice().findObject(By.text(arryButtonText[i]));
            if (mClickButton != null) {
                LogHelper.d(TAG, "[checkForCondition] find " + arryButtonText[i] + " click");
                mClickButton.click();
                hasbutton = true;
            }
        }
        return hasbutton;
    }
}
