LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MANIFEST_FILE := AndroidManifest.xml
LOCAL_JAVA_LIBRARIES := vivo-framework
LOCAL_SRC_FILES := $(call all-java-files-under, src) 
LOCAL_PACKAGE_NAME := Test3G4G
include $(BUILD_PACKAGE)
