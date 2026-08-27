package com.contoh.smartsidebar

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import rikka.shizuku.Shizuku
import kotlin.math.abs

data class AppItem(val name: String, val pkg: String, val command: String)

class SidebarService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var sidebarView: View
    private lateinit var params: WindowManager.LayoutParams
    
    private var isPanelOpen = false
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    // 4 APLIKASI UTAMA (WhatsApp, Chrome, YouTube, TikTok)
    private val appList = listOf(
        AppItem("WhatsApp", "com.whatsapp", "am start --windowingMode 5 -n com.whatsapp/.Main"),
        AppItem("Chrome", "com.android.chrome", "am start --windowingMode 5 -n com.android.chrome/com.google.android.apps.chrome.Main"),
        AppItem("YouTube", "com.google.android.youtube", "am start --windowingMode 5 -n com.google.android.youtube/com.google.android.apps.youtube.app.WatchWhileActivity"),
        AppItem("TikTok", "com.zhiliaoapp.musically", "am start --windowingMode 5 -p com.zhiliaoapp.musically")
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY 
    }

    override fun onCreate() {
        super.onCreate()
        
        val channelId = "SidebarChannel"
        val channel = NotificationChannel(channelId, "Smart Sidebar", NotificationManager.IMPORTANCE_MIN)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Smart Sidebar Aktif")
            .setContentText("Berjalan di latar belakang.")
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .build()
        startForeground(1, notification)

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

        // Generate Grid Aplikasi (4 Item: 2x2 rapi)
        val grid = sidebarView.findViewById<GridLayout>(R.id.app_grid)
        val pm = packageManager

        for (app in appList) {
            val itemLayout = LinearLayout(this)
            itemLayout.orientation = LinearLayout.VERTICAL
            itemLayout.gravity = Gravity.CENTER
            
            val gridParams = GridLayout.LayoutParams()
            gridParams.width = 0
            gridParams.height = GridLayout.LayoutParams.WRAP_CONTENT
            gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            gridParams.setMargins(4, 12, 4, 12)
            itemLayout.layoutParams = gridParams

            val icon = ImageView(this)
            icon.layoutParams = LinearLayout.LayoutParams(85, 85)
            try {
                icon.setImageDrawable(pm.getApplicationIcon(app.pkg))
            } catch (e: Exception) {
                icon.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            val text = TextView(this)
            text.text = app.name
            text.setTextColor(Color.WHITE)
            text.textSize = 10f
            text.gravity = Gravity.CENTER
            text.setPadding(0, 6, 0, 0)

            itemLayout.addView(icon)
            itemLayout.addView(text)

            itemLayout.setOnClickListener {
                runShizuku(app.command)
                panel.visibility = View.GONE
                isPanelOpen = false
            }

            grid.addView(itemLayout)
        }

        // Logika Touch: Geser Atas-Bawah & Swipe 2x untuk Buka/Tutup
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
                    if (abs(deltaY) > 5) {
                        params.y = initialY + deltaY
                        windowManager.updateViewLayout(sidebarView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    if (abs(deltaX) > 20 && abs(deltaX) > abs(deltaY)) {
                        if (!isPanelOpen && deltaX > 20) {
                            isPanelOpen = true
                            panel.visibility = View.VISIBLE
                        } else if (isPanelOpen) {
                            isPanelOpen = false
                            panel.visibility = View.GONE
                        }
                    }
                    true
                }
                else -> false
            }
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
