package com.osv01d.client.mining

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object MiningPolicy {
    const val DEFAULT_ITERATIONS = 50_000
    const val MIN_ITERATIONS = 1_000
    const val MAX_ITERATIONS = 2_000_000
    fun clampIterations(value: Int) = value.coerceIn(MIN_ITERATIONS, MAX_ITERATIONS)
}

class MiningController(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun enable(): String {
        prefs.edit().putBoolean(KEY_ENABLED, true).apply()
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<TopologyMiningWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        return "Topology background compute enabled: charging + battery-not-low, 15-minute minimum cadence, bounded local CPU only."
    }

    fun disable(): String {
        prefs.edit().putBoolean(KEY_ENABLED, false).apply()
        WorkManager.getInstance(context).cancelUniqueWork(NAME)
        return "Topology background compute disabled."
    }

    fun runOnce(iterations: Int): String {
        val bounded = MiningPolicy.clampIterations(iterations)
        val data = Data.Builder().putBoolean("manual", true).putInt("iterations", bounded).build()
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<TopologyMiningWorker>().setInputData(data).build())
        return "Queued one local topology compute pass: iterations=$bounded"
    }

    fun status(): String {
        val enabled = prefs.getBoolean(KEY_ENABLED, false)
        val sequence = prefs.getLong(KEY_SEQUENCE, 0L)
        val last = prefs.getString(KEY_LAST, "none") ?: "none"
        return "TOPOLOGY_COMPUTE enabled=$enabled sequence=$sequence · non-crypto · local-only · last=${last.take(120)}"
    }

    companion object {
        const val PREFS = "ghostit_topology_compute"
        const val KEY_ENABLED = "enabled"
        const val KEY_SEQUENCE = "sequence"
        const val KEY_LAST = "last"
        const val NAME = "ghostit-topology-background"
    }
}
