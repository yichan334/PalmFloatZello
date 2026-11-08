package com.palm.floatzello

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import androidx.core.app.NotificationCompat

class FloatingService : Service() {
    private var windowManager: WindowManager? = null
    private var floatView: View? = null
    private var floatBtn: ImageButton? = null
    private var params: WindowManager.LayoutParams? = null
    private var downX = 0f
    private var downY = 0f
    private var startX = 0
    private var startY = 0
    private var isDragging = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notification: Notification = NotificationCompat.Builder(this, "floating_channel")
            .setContentTitle("Palm Float Zello")
            .setContentText("ปุ่มลอยกำลังทำงาน")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else { startForeground(1, notification) }
        } catch (e: Exception) { e.printStackTrace() }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatView = LayoutInflater.from(this).inflate(R.layout.floating_button, null)
        floatBtn = floatView?.findViewById(R.id.floatBtn)

        val prefs = getSharedPreferences("palm_prefs", MODE_PRIVATE)
        val size = prefs.getInt("size", 100)
        val alpha = prefs.getFloat("alpha", 1.0f)
        val locked = prefs.getBoolean("locked", false)
        val pxSize = (size * resources.displayMetrics.density).toInt()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else { @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE }

        params = WindowManager.LayoutParams(
            pxSize, pxSize, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("posX", 100)
            y = prefs.getInt("posY", 500)
        }
        floatView?.alpha = alpha

        floatView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY
                    startX = params!!.x; startY = params!!.y
                    isDragging = false
                    MyAccessibilityService.instance?.pressDown()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (!locked && (Math.abs(dx) > 15 || Math.abs(dy) > 15)) {
                        isDragging = true
                        MyAccessibilityService.instance?.pressUp()
                        params!!.x = startX + dx.toInt()
                        params!!.y = startY + dy.toInt()
                        windowManager?.updateViewLayout(floatView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    MyAccessibilityService.instance?.pressUp()
                    if (isDragging) {
                        prefs.edit().putInt("posX", params!!.x).putInt("posY", params!!.y).apply()
                    }
                    true
                }
                else -> false
            }
        }
        try { windowManager?.addView(floatView, params) } catch (e: Exception) { e.printStackTrace() }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("floating_channel", "Palm Float Zello", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { if (floatView != null) windowManager?.removeView(floatView) } catch (e: Exception) { e.printStackTrace() }
    }
}
