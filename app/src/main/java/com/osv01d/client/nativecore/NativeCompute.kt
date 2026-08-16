package com.osv01d.client.nativecore

object NativeCompute {
    fun mine(seed: String, iterations: Int): String = NativeTopologyCompute.mine(seed, iterations)
    fun ireeProbe(): String = NativeIreeRuntime.ireeProbe()
}

private object NativeTopologyCompute {
    init { System.loadLibrary("ghostit_compute") }
    external fun mine(seed: String, iterations: Int): String
}

private object NativeIreeRuntime {
    init { System.loadLibrary("ghostit_iree") }
    external fun ireeProbe(): String
}
