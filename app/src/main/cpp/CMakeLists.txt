cmake_minimum_required(VERSION 3.22.1)
project(ghostit_native LANGUAGES C CXX)

set(IREE_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/third_party/iree")
if(NOT EXISTS "${IREE_SOURCE_DIR}/CMakeLists.txt")
  message(FATAL_ERROR "IREE runtime source missing. Run tools/prepare_iree_runtime.sh before Gradle/Android Studio build.")
endif()

set(IREE_BUILD_COMPILER OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_SAMPLES OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_PYTHON_BINDINGS OFF CACHE BOOL "" FORCE)
set(IREE_ENABLE_RUNTIME_TRACING OFF CACHE BOOL "" FORCE)
set(IREE_HAL_DRIVER_DEFAULTS OFF CACHE BOOL "" FORCE)
set(IREE_HAL_DRIVER_LOCAL_SYNC ON CACHE BOOL "" FORCE)
set(IREE_HAL_DRIVER_LOCAL_TASK OFF CACHE BOOL "" FORCE)
set(IREE_HAL_DRIVER_VULKAN OFF CACHE BOOL "" FORCE)
set(IREE_HAL_EXECUTABLE_LOADER_DEFAULTS OFF CACHE BOOL "" FORCE)
set(IREE_HAL_EXECUTABLE_LOADER_VMVX_MODULE ON CACHE BOOL "" FORCE)

add_subdirectory("${IREE_SOURCE_DIR}" "${CMAKE_CURRENT_BINARY_DIR}/iree" EXCLUDE_FROM_ALL)

add_library(ghostit_compute SHARED native_compute.c)
target_link_libraries(ghostit_compute log)

add_library(ghostit_iree SHARED iree_jni.c)
target_link_libraries(ghostit_iree iree_runtime_runtime log)
