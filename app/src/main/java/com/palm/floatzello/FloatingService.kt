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
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import androidx.core.app.NotificationCompat

class FloatingService : Service() {
    private var windowManager: WindowManager? = null
    private var floatView: View? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notification: Notification = NotificationCompat.Builder(this, "floating_channel")
            .setContentTitle("Palm Float Zello")
            .setContentText("ปุ่มลอยกำลังทำงาน - กดทะลุไปโดน Zello ได้เลย")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(1, notification)
            }
        } catch (e: Exception) { e.printStackTrace() }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatView = LayoutInflater.from(this).inflate(R.layout.floating_button, null)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        }

        // สำคัญ: FLAG_NOT_TOUCHABLE = นิ้วทะลุผ่านไปโดน Zello ข้างใต้ได้เลย
        // FLAG_NOT_FOCUSABLE = ไม่แย่งโฟกัส
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 500
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
