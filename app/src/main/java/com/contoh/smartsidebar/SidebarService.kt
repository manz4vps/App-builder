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
import android.widget.Button
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
        params.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL

        sidebarView = LayoutInflater.from(this).inflate(R.layout.layout_sidebar, null)

        val handle = sidebarView.findViewById<View>(R.id.sidebar_handle)
        val panel = sidebarView.findViewById<View>(R.id.sidebar_panel)
        
        // Logika geser (Swipe) handle ke kanan
        handle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialX
                    if (deltaX > 50) {
                        isPanelOpen = true
                        panel.visibility = View.VISIBLE
                    } else if (deltaX < 10 && isPanelOpen) {
                        isPanelOpen = false
                        panel.visibility = View.GONE
                    }
                    true
                }
                else -> false
            }
        }

        // Tombol Aplikasi Floating
        sidebarView.findViewById<Button>(R.id.btn_wa).setOnClickListener {
            openApp("com.whatsapp", "com.whatsapp.Main")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        sidebarView.findViewById<Button>(R.id.btn_chrome).setOnClickListener {
            openApp("com.android.chrome", "com.google.android.apps.chrome.Main")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        sidebarView.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            openApp("com.android.settings", ".Settings")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        windowManager.addView(sidebarView, params)
    }

    private fun openApp(packageName: String, activityName: String) {
        val command = "am start --windowingMode 5 -n $packageName/$activityName"
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
