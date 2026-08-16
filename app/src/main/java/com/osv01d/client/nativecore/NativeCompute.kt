package com.osv01d.client.nativecore

object NativeCompute {
    init {
        System.loadLibrary("ghostit_compute")
        System.loadLibrary("ghostit_iree")
    }

    external fun mine(seed: String, iterations: Int): String
    external fun ireeProbe(): String
}
