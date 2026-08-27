package com.contoh.smartsidebar

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import rikka.shizuku.Shizuku

class SidebarService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var sidebarView: View
    private var isPanelOpen = false
    private var initialX = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.START or Gravity.CENTER_VERTICAL

        sidebarView = LayoutInflater.from(this).inflate(R.layout.layout_sidebar, null)

        val handle = sidebarView.findViewById<View>(R.id.sidebar_handle)
        val panel = sidebarView.findViewById<View>(R.id.sidebar_panel)

        // Deteksi SWIPE Kiri ke Kanan
        handle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialX
                    if (deltaX > 20) { // Geser sedikit ke kanan langsung kebuka
                        isPanelOpen = true
                        panel.visibility = View.VISIBLE
                    } else if (deltaX < -10 && isPanelOpen) {
                        isPanelOpen = false
                        panel.visibility = View.GONE
                    }
                    true
                }
                else -> false
            }
        }

        // Action Klik WhatsApp
        sidebarView.findViewById<View>(R.id.btn_wa).setOnClickListener {
            openAppFloating("com.whatsapp")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        // Action Klik Chrome
        sidebarView.findViewById<View>(R.id.btn_chrome).setOnClickListener {
            openAppFloating("com.android.chrome")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        // Action Klik Settings
        sidebarView.findViewById<View>(R.id.btn_settings).setOnClickListener {
            openAppFloating("com.android.settings")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        windowManager.addView(sidebarView, params)
    }

    // Mantra Pemanggil Windowing Mode Floating via Shizuku (Fix 100% Buka)
    private fun openAppFloating(packageName: String) {
        val command = "am start --windowingMode 5 -n $(cmd package resolve-activity --brief $packageName | tail -n 1)"
        try {
            if (Shizuku.pingBinder()) {
                Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sidebarView.isInitialized) {
            windowManager.removeView(sidebarView)
        }
    }
}
