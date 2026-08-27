package com.contoh.smartsidebar

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import rikka.shizuku.Shizuku

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TOMBOL 1: Izin Overlay
        findViewById<Button>(R.id.btn_req_overlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                Toast.makeText(this, "Izin Overlay sudah Ok!", Toast.LENGTH_SHORT).show()
            }
        }

        // TOMBOL 2: Minta Izin Shizuku
        findViewById<Button>(R.id.btn_req_shizuku).setOnClickListener {
            try {
                if (Shizuku.pingBinder()) {
                    if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                        Shizuku.requestPermission(100)
                    } else {
                        Toast.makeText(this, "Izin Shizuku sudah Ok!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Shizuku belum aktif!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // TOMBOL 3: Jalankan Sidebar (Sekaligus kita bikin OTOMATIS JALAN)
        findViewById<Button>(R.id.btn_start_service).setOnClickListener {
            startSidebarService()
        }

        // OTOMATIS JALAN: Kalau izin overlay udah beres, aplikasi langsung otomatis 
        // aktifin sidebar dan keluar sendiri dari menu utama!
        if (Settings.canDrawOverlays(this)) {
            startSidebarService()
        }
    }

    private fun startSidebarService() {
        val serviceIntent = Intent(this, SidebarService::class.java)
        startForegroundService(serviceIntent)
        finish() // Langsung tutup halaman menu utamanya biar ga mengganggu
    }
}
