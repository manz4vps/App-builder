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
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Izin Overlay sudah Ok!", Toast.LENGTH_SHORT).show()
            }
        }

        // TOMBOL 2: Minta Izin Shizuku (LANGSUNG TEMBAK!)
        findViewById<Button>(R.id.btn_req_shizuku).setOnClickListener {
            try {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(100)
                } else {
                    Toast.makeText(this, "Izin Shizuku sudah Ok!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Kalau error, berarti aplikasi harus di-restart (dibuang dari recent apps)
                Toast.makeText(this, "Tutup dulu aplikasinya dari Recent Apps, lalu buka lagi!", Toast.LENGTH_LONG).show()
            }
        }

        // TOMBOL 3: Jalankan Sidebar
        findViewById<Button>(R.id.btn_start_service).setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, SidebarService::class.java))
                finish() 
            } else {
                Toast.makeText(this, "Pencet Tombol 1 dulu Bro!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
