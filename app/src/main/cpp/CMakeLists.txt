cmake_minimum_required(VERSION 3.26)
project(ghostit_native LANGUAGES C CXX)

set(IREE_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/third_party/iree")
if(NOT EXISTS "${IREE_SOURCE_DIR}/CMakeLists.txt")
  message(FATAL_ERROR "IREE runtime source missing. Run tools/prepare_iree_runtime.sh before Gradle/Android Studio build.")
endif()

# IREE requires native host tools while cross-compiling (notably
# iree-flatcc-cli for generated runtime schemas). Build them on the host first
# and pass their directory through the environment.
if(DEFINED ENV{IREE_HOST_BIN_DIR} AND NOT "$ENV{IREE_HOST_BIN_DIR}" STREQUAL "")
  set(IREE_HOST_BIN_DIR "$ENV{IREE_HOST_BIN_DIR}" CACHE STRING "IREE native host tools" FORCE)
elseif(CMAKE_CROSSCOMPILING)
  message(FATAL_ERROR "IREE_HOST_BIN_DIR is required for the Android runtime build. Run tools/prepare_iree_host_tools.sh first.")
endif()

# Runtime-only IREE integration. The host compiler comes from the pinned Python
# package and is intentionally not built into Android.
set(IREE_BUILD_COMPILER OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_SAMPLES OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_BENCHMARKS OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_PYTHON_BINDINGS OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_BINDINGS_TFLITE OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_BINDINGS_TFLITE_JAVA OFF CACHE BOOL "" FORCE)
set(IREE_BUILD_ALL_CHECK_TEST_MODULES OFF CACHE BOOL "" FORCE)
set(IREE_ENABLE_RUNTIME_TRACING OFF CACHE BOOL "" FORCE)
set(IREE_ENABLE_CPUINFO OFF CACHE BOOL "" FORCE)
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
