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
import rikka.shizuku.Shizuku

class SidebarService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var sidebarView: View

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Aturan buat Floating Window (Bisa ditimpa di atas app lain)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        // Posisi handle di kiri layar
        params.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL

        // Nanti lu bikin file app/src/main/res/layout/layout_sidebar.xml
        // sidebarView = LayoutInflater.from(this).inflate(R.layout.layout_sidebar, null)
        
        // Contoh kalau udah ada tombol WhatsApp di layout lu:
        /*
        val btnWa = sidebarView.findViewById<Button>(R.id.btn_whatsapp)
        btnWa.setOnClickListener {
            openFloatingAppWithShizuku("com.whatsapp", "com.whatsapp.Main")
        }
        */
        
        // Pasang ke layar
        // windowManager.addView(sidebarView, params)
    }

    // Fungsi sakti buka app via Shizuku
    private fun openFloatingAppWithShizuku(packageName: String, activityName: String) {
        val command = "am start --windowingMode 5 -n $packageName/$activityName"
        
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            try {
                Shizuku.newProcess(arrayOf("sh", "-c", command), null, null).waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sidebarView.isInitialized) windowManager.removeView(sidebarView)
    }
}
