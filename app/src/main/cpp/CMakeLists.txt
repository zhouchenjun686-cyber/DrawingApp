cmake_minimum_required(VERSION 3.22.1)
project("share_helper")

# 生成 C++ 动态库
add_library(share_helper SHARED share_helper.cpp)

# 链接 Android 日志库（可选，方便调试）
target_link_libraries(share_helper log)
