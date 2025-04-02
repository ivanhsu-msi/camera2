/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2019. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/* DO NOT EDIT THIS FILE - it is machine generated */
/* Header for class com_mediatek_camera_feature_mode_ai_utils_NativeImageDetect.h */

#ifndef _Included_com_mediatek_camera_feature_mode_ai_utils_NativeImageDetect
#define _Included_com_mediatek_camera_feature_mode_ai_utils_NativeImageDetect

#include <jni.h>
#include <string>
#include <iomanip>
#include <sstream>
#include <fcntl.h>

#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <sys/mman.h>
#include <unistd.h>
#include <vector>

#ifdef __cplusplus
extern "C" {
#endif
char *jStringToChar(JNIEnv *env, jstring jstr);

JNIEXPORT void JNICALL
Java_com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect_init
        (JNIEnv *env, jclass clazz);

JNIEXPORT jint JNICALL
Java_com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect_initModel
        (JNIEnv *env, jclass clazz, jint model_type, jstring model_path, jstring label_path, jint detect_count);

JNIEXPORT jint JNICALL
Java_com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect_initModels
        (JNIEnv *env, jclass clazz, jint model_type, jobject asset_manager, jstring model_name,
         jobject label_list, jint detect_count);

/*
 * Class:     com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect
 * Method:    initModelsByPath
 * Signature: (ILjava/lang/String;Ljava/lang/Object;)I;
 */
JNIEXPORT jint JNICALL
Java_com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect_initModelsByPath(
        JNIEnv *env, jclass clazz, jint model_type, jstring model_path, jobject label_list, jint detect_count);

JNIEXPORT jint JNICALL
Java_com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect_initModelList(
        JNIEnv *env, jclass clazz, jint model_type, jobject label_list, jint detect_count);
/*
 * Class:     com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect
 * Method:    compute
 * Signature: (Ljava/lang/Object;J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect_compute
        (JNIEnv *env, jclass clazz, jobject image_data, jlong size, jint model_type);

JNIEXPORT jbyteArray JNICALL
Java_com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect_encodeRSAPrivateKey(
        JNIEnv *env, jclass clazz, jbyteArray src);

JNIEXPORT jbyteArray JNICALL
Java_com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect_decodeRSAPubKey(
        JNIEnv *env, jclass clazz, jbyteArray src);

JNIEXPORT void JNICALL
Java_com_mediatek_camera_feature_mode_visualsearch_utils_NativeImageDetect_destroyModel
        (JNIEnv *env, jclass clazz);

#ifdef __cplusplus
}
#endif
#endif
