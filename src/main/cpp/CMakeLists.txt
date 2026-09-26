cmake_minimum_required(VERSION 3.10)
project(tickodium_native)

# Use modern C++ standard
set(CMAKE_CXX_STANDARD 17)

# Search for the Java Native Interface (JNI) library
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

# Compile the C++ file into a shared library (.dll or .so)
add_library(tickodium_native SHARED tickodium_native.cpp)
