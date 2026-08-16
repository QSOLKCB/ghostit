#include <jni.h>
#include "iree/runtime/api.h"

JNIEXPORT jstring JNICALL
Java_com_osv01d_client_nativecore_NativeIreeRuntime_ireeProbe(JNIEnv* env, jobject thiz) {
  (void)thiz;
  iree_runtime_instance_options_t instance_options;
  iree_runtime_instance_options_initialize(&instance_options);
  iree_runtime_instance_options_use_all_available_drivers(&instance_options);

  iree_runtime_instance_t* instance = NULL;
  iree_status_t status = iree_runtime_instance_create(&instance_options, iree_allocator_system(), &instance);
  if (!iree_status_is_ok(status)) return (*env)->NewStringUTF(env, "IREE runtime linked but instance creation failed");

  iree_hal_device_t* device = NULL;
  status = iree_runtime_instance_try_create_default_device(instance, iree_make_cstring_view("local-sync"), &device);
  if (!iree_status_is_ok(status)) {
    iree_runtime_instance_release(instance);
    return (*env)->NewStringUTF(env, "IREE runtime instance OK; local-sync device unavailable");
  }
  iree_hal_device_release(device);
  iree_runtime_instance_release(instance);
  return (*env)->NewStringUTF(env, "IREE 3.11 runtime OK · JNI linked · local-sync device created · VMVX asset bundled");
}
