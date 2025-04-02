ifneq ($(strip $(MSSI_MTK_TC1_COMMON_SERVICE)), yes)
LOCAL_ROOT_PATH:= $(call my-dir)

include $(LOCAL_ROOT_PATH)/host/Android.mk
include $(LOCAL_ROOT_PATH)/portability/Android.mk
include $(LOCAL_ROOT_PATH)/tests/Android.mk
include $(LOCAL_ROOT_PATH)/aovtest/Android.mk
include $(LOCAL_ROOT_PATH)/testscat/Android.mk
include $(LOCAL_ROOT_PATH)/feature/mode/visualsearch/jni/Android.mk

endif
