package com.mediatek.camera.tests.v3.operator;

import com.mediatek.camera.tests.helper.LoggerService;
import com.mediatek.camera.tests.v3.arch.OperatorOne;
import com.mediatek.camera.tests.v3.arch.Page;

public class ForceEnable3DNROperator extends OperatorOne {
    @Override
    public Page getPageBeforeOperate() {
        return null;
    }

    @Override
    public Page getPageAfterOperate() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Force enable 3DNR by set system property";
    }

    @Override
    protected void doOperate() {
        LoggerService mLoggerService = LoggerService.getInstance();
        mLoggerService.setAdbCommand("vendor.debug.camera.3dnr.debug.enable", "1");
        mLoggerService.setAdbCommand("vendor.debug.camera.3dnr.enable", "n");
        mLoggerService.setAdbCommand("vendor.debug.3dnr.iso.threshold", "0");
        mLoggerService.setAdbCommand("vendor.debug.camera.3dnr.log.level", "2");
    }
}
