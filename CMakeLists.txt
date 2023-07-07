cmake_minimum_required(VERSION 3.10)

project(my_project)

# using C++14
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_COMPILER "/usr/bin/clang++")
set(CMAKE_CXX_FLAGS
    -stdlib=libc++
    # -lc++abi
)
include_directories(/usr/lib/jvm/java-11-openjdk-amd64/include)
include_directories(/usr/lib/jvm/java-11-openjdk-amd64/include/linux)
aux_source_directory(. SRC_LIST) #把当前目录(.)下所有源代码文件和头文件加入变量SRC_LIST
add_executable(test ${SRC_LIST}) #生成应用程序

add_library(utils SHARED ${SRC_LIST})
set_target_properties(utils PROPERTIES output_name "utils")
