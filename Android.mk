LOCAL_PATH:= $(call my-dir)

# prebuilt java libraries
include $(CLEAR_VARS)

# TODO: use the $(TARGET_OUT_JAVA_LIBRARIES)/mrvltv-featuremanager.jar

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := pinyin4j:libs/pinyin4j-2.5.0.jar \
	mrvltv_featuremanager_prebuilt:libs/mrvltv-featuremanager.jar

include $(BUILD_MULTI_PREBUILT)

# EPG applicaion
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := EPG

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
    $(call all-java-files-under, src)

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PROGUARD_ENABLED := disabled

#LOCAL_PROGUARD_FLAGS := -include $(LOCAL_PATH)/proguard.flags

LOCAL_STATIC_JAVA_LIBRARIES := pinyin4j \
   mrvltv_featuremanager_prebuilt

include $(BUILD_PACKAGE)
