package com.contoh.smartsidebar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Kalau HP baru selesai nyala, otomatis jalankan Sidebar!
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            if (Settings.canDrawOverlays(context)) {
                val serviceIntent = Intent(context, SidebarService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
