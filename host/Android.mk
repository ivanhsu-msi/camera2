LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := com.mediatek.camera.portability
LOCAL_STATIC_JAVA_LIBRARIES += guava
LOCAL_STATIC_JAVA_LIBRARIES += appluginmanager
LOCAL_STATIC_JAVA_LIBRARIES += vendor.mediatek.hardware.camera.bgservice-V1.0-java

LOCAL_STATIC_JAVA_LIBRARIES += androidx.appcompat_appcompat
LOCAL_STATIC_JAVA_LIBRARIES += androidx.recyclerview_recyclerview
LOCAL_STATIC_JAVA_LIBRARIES += androidx.annotation_annotation

LOCAL_RENDERSCRIPT_TARGET_API := 18
LOCAL_RENDERSCRIPT_COMPATIBILITY := 18

LOCAL_JNI_SHARED_LIBRARIES := libnn_sample libimage_detect

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res $(LOCAL_PATH)/../feature/setting/cameraswitcher/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/androidx/m2repository/androidx/appcompat/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/tpi/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/ainr/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/continuousshot/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/hdr/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/standardhdr10/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/flash/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/focus/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/exposure/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/zoom/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/dng/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/dualcamerazoom/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/selftimer/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/facedetection/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/objecttracking/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/picturesize/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/previewmode/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/microphone/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/hdr10/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/videoquality/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/vsdofquality/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/videoformat/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/noisereduction/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/fps60/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/eis/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/ais/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/scenemode/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/whitebalance/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/antiflicker/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/zsd/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/iso/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/aaaroidebug/res

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/shutterspeed/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/longexposure/res

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/slowmotion/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/vsdof/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/formats/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/slowmotionquality/res

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/qrcode/res

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/visualsearch/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/mode/visualsearch/res
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../feature/setting/zoommanualing/res


LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-renderscript-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/tpi/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/ainr/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/afbc/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/cameraswitcher/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/continuousshot/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/hdr/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/visualsearch/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/flash/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/focus/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/exposure/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/standardhdr10/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/zoom/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/facedetection/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/objecttracking/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/dng/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/dualcamerazoom/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/selftimer/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/picturesize/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/previewmode/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/microphone/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/hdr10/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/videoquality/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/vsdofquality/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/videoformat/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/noisereduction/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/fps60/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/eis/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/ais/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/scenemode/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/whitebalance/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/antiflicker/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/zsd/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/iso/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/aaaroidebug/src)

LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/shutterspeed/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/longexposure/src)

LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/slowmotion/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/vsdof/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/formats/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/slowmotionquality/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/postview/src)
LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/setting/zoommanualing/src)

LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/qrcode/src)

LOCAL_SRC_FILES += $(call all-java-files-under, ../feature/mode/visualsearch/src)


LOCAL_SRC_FILES += $(call all-java-files-under, ../common/src)
LOCAL_SRC_FILES += $(call all-Iaidl-files-under, ../feature/mode/visualsearch/aidl)
LOCAL_SRC_FILES += ../feature/mode/visualsearch/aidl/com/visualsearch/DataInterface.aidl
LOCAL_AIDL_INCLUDES := $(LOCAL_PATH)/../feature/mode/visualsearch/aidl

#LOCAL_ASSET_FILES += $(call find-subdir-assets)
#LOCAL_ASSET_DIR += $(LOCAL_PATH)/../feature/mode/visualsearch/assets
#LOCAL_AAPT_FLAGS := -0 .tflite

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --no-version-vectors
LOCAL_AAPT_FLAGS += --extra-packages androidx.appcompat

LOCAL_MIN_SDK_VERSION := 21

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_PACKAGE_NAME := Camera
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_SYSTEM_EXT_MODULE := true
#overrides aosp camera
LOCAL_OVERRIDES_PACKAGES := Camera2
include $(BUILD_PACKAGE)
