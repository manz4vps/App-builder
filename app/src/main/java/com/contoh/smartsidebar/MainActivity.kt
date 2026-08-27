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

        findViewById<Button>(R.id.btn_req_overlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                Toast.makeText(this, "Izin Overlay sudah Ok!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_req_shizuku).setOnClickListener {
            try {
                if (Shizuku.pingBinder()) {
                    if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                        Shizuku.requestPermission(100)
                    } else {
                        Toast.makeText(this, "Izin Shizuku sudah Ok!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Menunggu Shizuku... Pastikan Shizuku Running!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

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
