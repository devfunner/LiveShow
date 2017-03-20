//
// Created by Administrator on 2017/3/18.
//
#include "com_liuyt_liveshow_TestJniUtils.h"
/*
 * Class:     com_liuyt_liveshow_TestJniUtils
 * Method:    getCLanguageString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_liuyt_liveshow_TestJniUtils_getCLanguageString
        (JNIEnv * env, jobject thiz)
{
    return (*env)->NewStringUTF(env,"developer!");
}