#include <jni.h>
#include <string>

// 必须与 Java 中的包名和类名完全对应
// Java_com_example_drawingapp_MainActivity_getShareText
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_drawingapp_MainActivity_getShareText(JNIEnv *env, jobject /* this */, jstring prefix) {
    // 1. 把 Java 传过来的字符串转成 C++ 的 string
    const char *prefixChars = env->GetStringUTFChars(prefix, nullptr);
    std::string prefixStr(prefixChars);
    env->ReleaseStringUTFChars(prefix, prefixChars);

    // 2. 拼接我们想要分享的文字（这里是 C++ 语法的核心逻辑）
    std::string result = "【来自C++的分享】" + prefixStr + " —— 体验一下我用Java+C++混合编程写的应用！";

    // 3. 把 C++ 的 string 转回 Java 能识别的字符串并返回
    return env->NewStringUTF(result.c_str());
}
