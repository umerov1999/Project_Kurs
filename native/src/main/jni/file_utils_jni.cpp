#include <jni.h>
#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <thread>
#include <iostream>
#include <list>

using namespace std;

extern "C" JNIEXPORT jint
Java_dev_umerov_project_module_FileUtils_getThreadsCountNative(JNIEnv *env, jobject) {
    return std::thread::hardware_concurrency();
}
