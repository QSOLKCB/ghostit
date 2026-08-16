package com.osv01d.client.mining

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.osv01d.client.nativecore.NativeCompute

class TopologyMiningWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(MiningController.PREFS, Context.MODE_PRIVATE)
        val manual = inputData.getBoolean("manual", false)
        if (!manual && !prefs.getBoolean(MiningController.KEY_ENABLED, false)) return Result.success()

        val iterations = MiningPolicy.clampIterations(inputData.getInt("iterations", MiningPolicy.DEFAULT_ITERATIONS))
        val sequence = synchronized(sequenceLock) {
            val next = prefs.getLong(MiningController.KEY_SEQUENCE, 0L) + 1L
            if (!prefs.edit().putLong(MiningController.KEY_SEQUENCE, next).commit()) return Result.failure()
            next
        }
        val seed = "ghostit-topology-$sequence"
        val output = runCatching { NativeCompute.mine(seed, iterations) }.getOrElse { return Result.failure() }
        prefs.edit().putString(MiningController.KEY_LAST, output).apply()
        return Result.success()
    }

    companion object {
        private val sequenceLock = Any()
    }
}
