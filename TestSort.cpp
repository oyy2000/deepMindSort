#include "TestSort.h"
#include "jni.h"

#include <algorithm>
JNIEXPORT jintArray JNICALL Java_TestSort_sortTest(JNIEnv *env, jobject obj, jintArray javaArr)
{
	// 获取Java数组长度
	int length = env->GetArrayLength(javaArr);

	// 根据Java数组创建C数组，也就是把Java数组转换成C数组
	int *arrp = env->GetIntArrayElements(javaArr, 0);

	std::sort(arrp, arrp + length);
	
	// 将C数组种的元素拷贝到Java数组中
	env->SetIntArrayRegion(javaArr, 0, length, arrp);
	return javaArr;
}

int main()
{
	
}