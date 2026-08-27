package com.contoh.smartsidebar

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tombol 1: Minta izin Overlay (System Alert Window)
        findViewById<Button>(R.id.btn_req_overlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Izin Overlay sudah Ok!", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol 2: Minta izin Shizuku
        findViewById<Button>(R.id.btn_req_shizuku).setOnClickListener {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(100)
                } else {
                    Toast.makeText(this, "Izin Shizuku sudah Ok!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Shizuku belum aktif di HP ini!", Toast.LENGTH_LONG).show()
            }
        }

        // Tombol 3: Jalanin Sidebar Service
        findViewById<Button>(R.id.btn_start_service).setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, SidebarService::class.java))
                finish() // Langsung tutup aplikasi biar rapi, sidebar jalan di background
            } else {
                Toast.makeText(this, "Pencet Tombol 1 dulu Bro!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
