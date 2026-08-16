#include <jni.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

static uint64_t mix64(uint64_t x) {
  x ^= x >> 30; x *= UINT64_C(0xbf58476d1ce4e5b9);
  x ^= x >> 27; x *= UINT64_C(0x94d049bb133111eb);
  return x ^ (x >> 31);
}

static uint64_t seed_hash(const char* s) {
  uint64_t h = UINT64_C(1469598103934665603);
  for (; *s; ++s) { h ^= (unsigned char)*s; h *= UINT64_C(1099511628211); }
  return h;
}

JNIEXPORT jstring JNICALL
Java_com_osv01d_client_nativecore_NativeTopologyCompute_mine(JNIEnv* env, jobject thiz, jstring seed, jint iterations) {
  (void)thiz;
  const char* chars = (*env)->GetStringUTFChars(env, seed, NULL);
  if (!chars) return NULL;
  int count = iterations < 1 ? 1 : (iterations > 2000000 ? 2000000 : iterations);
  uint64_t base = seed_hash(chars), best = 0, best_nonce = 0;
  for (uint64_t nonce = 0; nonce < (uint64_t)count; ++nonce) {
    uint64_t score = mix64(base ^ nonce);
    if (score > best) { best = score; best_nonce = nonce; }
  }
  (*env)->ReleaseStringUTFChars(env, seed, chars);
  char out[160];
  snprintf(out, sizeof(out), "native-topology nonce=%llu score=%016llx iterations=%d", (unsigned long long)best_nonce, (unsigned long long)best, count);
  return (*env)->NewStringUTF(env, out);
}
