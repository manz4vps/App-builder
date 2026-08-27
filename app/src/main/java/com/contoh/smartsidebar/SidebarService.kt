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

        // Logika Swipe Handle
        handle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialX
                    if (deltaX > 20) {
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

        // 1. Tombol WhatsApp (Langsung tembak target activity-nya)
        sidebarView.findViewById<View>(R.id.btn_wa).setOnClickListener {
            runCommand("am start --windowingMode 5 -n com.whatsapp/com.whatsapp.Home")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        // 2. Tombol Chrome
        sidebarView.findViewById<View>(R.id.btn_chrome).setOnClickListener {
            runCommand("am start --windowingMode 5 -n com.android.chrome/com.google.android.apps.chrome.Main")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        // 3. Tombol Settings (Persis kaya yang lu sebutin!)
        sidebarView.findViewById<View>(R.id.btn_settings).setOnClickListener {
            runCommand("am start --windowingMode 5 -n com.android.settings/.Settings")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        windowManager.addView(sidebarView, params)
    }

    // Eksekusi command langsung via Shizuku tanpa syarat ribet
    private fun runCommand(command: String) {
        try {
            Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
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
