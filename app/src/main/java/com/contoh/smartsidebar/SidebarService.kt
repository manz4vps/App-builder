package com.contoh.smartsidebar

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import rikka.shizuku.Shizuku

class SidebarService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var sidebarView: View
    private var isPanelOpen = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Setup Window Manager untuk Floating Layout
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL

        // Load layout sidebar yang dibikin di XML tadi
        sidebarView = LayoutInflater.from(this).inflate(R.layout.layout_sidebar, null)

        val handle = sidebarView.findViewById<View>(R.id.sidebar_handle)
        val panel = sidebarView.findViewById<View>(R.id.sidebar_panel)
        val btnWa = sidebarView.findViewById<Button>(R.id.btn_wa)
        val btnChrome = sidebarView.findViewById<Button>(R.id.btn_chrome)
        val btnSettings = sidebarView.findViewById<Button>(R.id.btn_settings)

        // Logika Buka Tutup Panel pas Handle dipencet
        handle.setOnClickListener {
            isPanelOpen = !isPanelOpen
            panel.visibility = if (isPanelOpen) View.VISIBLE else View.GONE
        }

        // Tombol WhatsApp (Asumsi activity utama com.whatsapp.Main)
        btnWa.setOnClickListener {
            openFloatingAppWithShizuku("com.whatsapp", "com.whatsapp.Main")
            panel.visibility = View.GONE // Tutup panel otomatis
            isPanelOpen = false
        }

        // Tombol Chrome
        btnChrome.setOnClickListener {
            openFloatingAppWithShizuku("com.android.chrome", "com.google.android.apps.chrome.Main")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        // Tombol Settings
        btnSettings.setOnClickListener {
            openFloatingAppWithShizuku("com.android.settings", ".Settings")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        // Tempel UI ke layar
        windowManager.addView(sidebarView, params)
    }

    private fun openFloatingAppWithShizuku(packageName: String, activityName: String) {
        val command = "am start --windowingMode 5 -n $packageName/$activityName"
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            try {
                Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sidebarView.isInitialized) {
            windowManager.removeView(sidebarView)
        }
    }
}
