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
import android.widget.ImageView
import rikka.shizuku.Shizuku
import kotlin.math.abs

class SidebarService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var sidebarView: View
    private lateinit var params: WindowManager.LayoutParams
    
    private var isPanelOpen = false
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        params = WindowManager.LayoutParams(
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

        try {
            val pm = packageManager
            sidebarView.findViewById<ImageView>(R.id.ic_wa).setImageDrawable(pm.getApplicationIcon("com.whatsapp"))
            sidebarView.findViewById<ImageView>(R.id.ic_chrome).setImageDrawable(pm.getApplicationIcon("com.android.chrome"))
            sidebarView.findViewById<ImageView>(R.id.ic_settings).setImageDrawable(pm.getApplicationIcon("com.android.settings"))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // LOGIKA BARU: Bisa digeser atas/bawah, dan di-KLIK/SWIPE KANAN untuk Buka-Tutup
        handle.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    // Hanya geser kalau jari gerak lumayan jauh (biar nggak bentrok sama klik)
                    if (abs(deltaY) > 10) {
                        params.y = initialY + deltaY
                        windowManager.updateViewLayout(sidebarView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    // Kalau jari cuma neken (Klik), otomatis Buka/Tutup panelnya!
                    if (abs(deltaX) < 10 && abs(deltaY) < 10) {
                        isPanelOpen = !isPanelOpen
                        panel.visibility = if (isPanelOpen) View.VISIBLE else View.GONE
                    } 
                    // Kalau sengaja digeser ke kanan -> Buka
                    else if (deltaX > 20) {
                        isPanelOpen = true
                        panel.visibility = View.VISIBLE
                    }
                    true
                }
                else -> false
            }
        }

        // KITA HAPUS --bounds BIAR APLIKASI LANCAR JAYA LAGI
        sidebarView.findViewById<View>(R.id.btn_wa).setOnClickListener {
            runShizuku("am start --windowingMode 5 -n com.whatsapp/.Main")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        sidebarView.findViewById<View>(R.id.btn_chrome).setOnClickListener {
            runShizuku("am start --windowingMode 5 -n com.android.chrome/com.google.android.apps.chrome.Main")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        sidebarView.findViewById<View>(R.id.btn_settings).setOnClickListener {
            runShizuku("am start --windowingMode 5 -n com.android.settings/.Settings")
            panel.visibility = View.GONE
            isPanelOpen = false
        }

        windowManager.addView(sidebarView, params)
    }

    private fun runShizuku(command: String) {
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
