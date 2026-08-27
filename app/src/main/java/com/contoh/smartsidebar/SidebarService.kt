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
            val action = event.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                initialX = event.rawX
                true
            } else if (action == MotionEvent.ACTION_UP) {
                val deltaX = event.rawX - initialX
                if (deltaX > 20) {
                    isPanelOpen = true
                    panel.visibility = View.VISIBLE
                } else if (deltaX < -10 && isPanelOpen) {
                    isPanelOpen = false
                    panel.visibility = View.GONE
                }
                true
            } else {
                false
            }
        }

        // Tombol WhatsApp (Pakai format andalan lu: com.whatsapp/.Main)
        sidebarView.findViewById<View>(R.id.btn_wa).setOnClickListener {
            executeShellCommand("am start --windowingMode 5 -n com.whatsapp/.Main")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        // Tombol Chrome
        sidebarView.findViewById<View>(R.id.btn_chrome).setOnClickListener {
            executeShellCommand("am start --windowingMode 5 -n com.android.chrome/com.google.android.apps.chrome.Main")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        // Tombol Settings
        sidebarView.findViewById<View>(R.id.btn_settings).setOnClickListener {
            executeShellCommand("am start --windowingMode 5 -n com.android.settings/.Settings")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        windowManager.addView(sidebarView, params)
    }

    private fun executeShellCommand(command: String) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            process.waitFor()
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
