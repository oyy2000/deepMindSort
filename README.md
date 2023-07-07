# DeepMind hash() 与sort() 的JNI封装性能对比

# 参考
- https://www.nature.com/articles/s41586-023-06004-9
- https://www.deepmind.com/blog/alphadev-discovers-faster-sorting-algorithms

# 环境

```c#
OS: WSL Ubuntu 22.04
JVM: JDK 11.0.18
Clang++: Ubuntu clang version 14.0.0-1ubuntu1
C++库: LLVM libc++ 15
```

# 编译运行

1. 在Ubuntu上安装LLVM编译器， clang++编译前端，以及含有论文中sort代码的libc++15。
2. 写Java代码包含native的sortTest方法，并生成C++的头文件
```java
public class TestSort {
    public native int[] sortTest(int[] arr);
    static {
        System.loadLibrary("utils");
    }
}

javac -h ./ TestSort.java 
```
3. 创建TestSort.cpp引用生成的头文件，并且编写方法体。
```c++
#include "TestSort.h"
#include "jni.h"

#include <iostream>
#include <algorithm>
#include <chrono>
using namespace std::chrono;
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
```
4. 创建CMakeLists.txt 文件如下，注意clang++和命令行参数-stdlib=libc++的使用，这样才能保证用到我们所需的代码。还要include JDK的代码确保jni.h被引入。
```bash
cmake_minimum_required(VERSION 3.10)

project(my_project)

# using C++14
set(CMAKE_CXX_STANDARD 14)
# set (CMAKE_C_COMPILER "/usr/local/gcc/bin/gcc")
set (CMAKE_CXX_COMPILER "/usr/bin/clang++")
set(CMAKE_CXX_FLAGS
    -stdlib=libc++
    # -lc++abi
)
# Process Abseil's CMake build system
# add_subdirectory(abseil-cpp)
include_directories(/usr/lib/jvm/java-11-openjdk-amd64/include)
include_directories(/usr/lib/jvm/java-11-openjdk-amd64/include/linux)
# add_executable(test TestSort.cpp)
AUX_SOURCE_DIRECTORY(. SRC_LIST) #把当前目录(.)下所有源代码文件和头文件加入变量SRC_LIST
ADD_EXECUTABLE(test ${SRC_LIST}) #生成应用程序

add_library(utils SHARED ${SRC_LIST})
set_target_properties(utils PROPERTIES output_name "utils")
```
5. 在当前目录新建一个build文件夹，进入并且编译c++代码为.so的动态链接库
```bash
cmake ..
```
6. 引入动态链接库，开发代码测试
```java
import java.util.Arrays;

public class TestSort {
    public native int[] sortTest(int[] arr);

    private final static int LOOP_TIME = 10;

    static {
        System.loadLibrary("utils");
    }

    public long testCxxSort(int len, int minValue, int maxValue) {
        long totalTime = 0;

        for (int i = 0; i < LOOP_TIME; i++) {
            int[] arr = randomGen(len, minValue, maxValue);

            long millis1 = System.nanoTime();
            int[] sorted = sortTest(arr);
            long millis2 = System.nanoTime();

            // Check if the array is correctly sorted
            if (!isSorted(sorted)) {
                System.out.println("Wrong!");
                break;
            }

            totalTime += millis2 - millis1;
        }

        long averageTime = totalTime / LOOP_TIME;
        System.out.println(len + " elements array sorted by C++ takes an average time of " + averageTime + " ns");
        return averageTime;
    }

    public long testJavaSort(int len, int minValue, int maxValue) {
        long totalTime = 0;

        for (int i = 0; i < LOOP_TIME; i++) {
            int[] arr = randomGen(len, minValue, maxValue);

            long millis1 = System.nanoTime();
            Arrays.sort(arr);
            long millis2 = System.nanoTime();

            // Check if the array is correctly sorted
            if (!isSorted(arr)) {
                System.out.println("Wrong!");
                break;
            }

            totalTime += millis2 - millis1;
        }

        long averageTime = totalTime / LOOP_TIME;
        System.out.println(len + " elements array sorted by Java takes an average time of " + averageTime + " ns");
        return averageTime;
    }

    public int[] randomGen(int number, int minValue, int maxValue) {
        int[] arr = new int[number];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = getRandomNumber(minValue, maxValue);
        }
        return arr;
    }

    private int getRandomNumber(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    private boolean isSorted(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        TestSort testSort = new TestSort();
        
        long first_time1000000 = testSort.testCxxSort(1000000, 1, 10000000);
        long second_time1000000 = testSort.testCxxSort(1000000, 1, 10000000);
        long third_time1000000 = testSort.testCxxSort(1000000, 1, 100000);
        long time3 = testSort.testCxxSort(3, 1, 1000);
        long time4 = testSort.testCxxSort(4, 1, 1000);
        long time5 = testSort.testCxxSort(5, 1, 1000);
        long time100 = testSort.testCxxSort(100, 1, 1000);
        long time1000 = testSort.testCxxSort(1000, 1, 10000);
        long time10000 = testSort.testCxxSort(10000, 1, 100000);
        long time100000 = testSort.testCxxSort(100000, 1, 1000000);
        long time1000000 = testSort.testCxxSort(1000000, 1, 10000000);
        
        long first_jtime1000000 = testSort.testJavaSort(100000, 1, 10000000);
        long second_jtime1000000 = testSort.testJavaSort(100000, 1, 10000000);
        long third_jtime1000000 = testSort.testJavaSort(100000, 1, 10000000);
        long jtime3 = testSort.testJavaSort(3, 1, 1000);
        long jtime4 = testSort.testJavaSort(4, 1, 1000);
        long jtime5 = testSort.testJavaSort(5, 1, 1000);
        long jtime100 = testSort.testJavaSort(100, 1, 1000);
        long jtime1000 = testSort.testJavaSort(1000, 1, 10000);
        long jtime10000 = testSort.testJavaSort(10000, 1, 100000);
        long jtime100000 = testSort.testJavaSort(100000, 1, 1000000);
        long jtime1000000 = testSort.testJavaSort(1000000, 1, 10000000);
    }
}
```

# 对比结果：

|元素个数|JNI耗时(ns)|Java耗时(ns)|C++原生耗时(ns)|JNI相比Java耗时(%)|
|:----|:----|:----|:----|:----|
|3|435  |203  |96.2 |114.29%|
|4|176 |190|96.2|-7.37%|
|5|249 |191|93.1 |30.37%|
|100|2874 |1839|1554  |56.28%|
|1000|25576  |37238 |23646.4 |-31.32%|
|10000|324191 |368264  |318590 |-11.97%|
|100000|4270275 |4517819 |4119830|-5.48%|
|1000000|49559037 |65513498|47710200|-24.35%|




