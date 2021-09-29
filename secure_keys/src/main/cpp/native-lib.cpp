#include <jni.h>
#include <string>

// Naming convention : Java_pkg-name_class-name_function-name

extern "C" {

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getMixpanelProdToken(JNIEnv *env, jobject) {
    return env->NewStringUTF("e83a9b4a18a938e68053d79329a48af8");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getMixpanelStagingToken(JNIEnv *env, jobject) {
    return env->NewStringUTF("b8f375c8df48bb563b24d19af10ffe58");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getMixpanelAlphaToken(JNIEnv *env, jobject) {
    return env->NewStringUTF("b128a18c8ba6eefc6b4290be93470a03");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getBugfenderToken(JNIEnv *env, jobject) {
    return env->NewStringUTF("yxTIoe2rQKj4JcNnpZB1FHZvXX6f57uH");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getAppsFlyerKey(JNIEnv *env, jobject) {
    return env->NewStringUTF("NVpChrDgyvouKoRp9HmgHH");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getCleverTapAccountIdStaging(JNIEnv *env, jobject) {
    return env->NewStringUTF("TEST-447-999-955Z");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getCleverTapTokenStaging(JNIEnv *env, jobject) {
    return env->NewStringUTF("TEST-ccc-a44");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getCleverTapAccountIdAlpha(JNIEnv *env, jobject) {
    return env->NewStringUTF("TEST-6Z8-85W-KR6Z");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getCleverTapTokenAlpha(JNIEnv *env, jobject) {
    return env->NewStringUTF("TEST-05b-b16");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getCleverTapAccountIdProd(JNIEnv *env, jobject) {
    return env->NewStringUTF("W6Z-866-4Z5Z");
}

JNIEXPORT jstring JNICALL
Java_tech_okcredit_secure_1keys_KeyProvider_getCleverTapTokenProd(JNIEnv *env, jobject) {
    return env->NewStringUTF("66b-160");
}

}