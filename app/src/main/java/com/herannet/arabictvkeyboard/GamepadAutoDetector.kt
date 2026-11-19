package com.herannet.arabictvkeyboard

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.provider.Settings

object GamepadAutoDetector {
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun usageSettingsIntent(context: Context) =
        android.content.Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun isConfiguredGameForeground(context: Context, packages: Set<String>): Boolean {
        if (packages.isEmpty() || !hasUsageAccess(context)) return false
        val usage = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val events = usage.queryEvents(end - 10_000L, end) ?: return false
        val event = UsageEvents.Event()
        var foreground: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) foreground = event.packageName
        }
        return foreground in packages
    }

    fun configuredPackages(value: String): Set<String> =
        value.split(',', '\n', ' ', '\t').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}
