package com.palm.floatzello
import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.ImageButton
import androidx.core.app.NotificationCompat

class FloatingService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var view: View
    private var isTalking = false
    private var lastX=0; private var lastY=0; private var touchX=0f; private var touchY=0f

    override fun onCreate(){
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        view = LayoutInflater.from(this).inflate(R.layout.floating_button, null)
        val btn = view.findViewById<ImageButton>(R.id.floatBtn)
        val params = WindowManager.LayoutParams(
            Prefs.size, Prefs.size,
            if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START; params.x=100; params.y=300
        btn.alpha = Prefs.alpha
        btn.setOnTouchListener { _, ev ->
            when(ev.action){
                MotionEvent.ACTION_DOWN -> { touchX=ev.rawX; touchY=ev.rawY; lastX=params.x; lastY=params.y; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx=(ev.rawX-touchX).toInt(); val dy=(ev.rawY-touchY).toInt()
                    if(kotlin.math.abs(dx)>10 || kotlin.math.abs(dy)>10){ params.x=lastX+dx; params.y=lastY+dy; wm.updateViewLayout(view,params) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if(kotlin.math.abs(ev.rawX-touchX)<10 && kotlin.math.abs(ev.rawY-touchY)<10){
                        if(Prefs.lockMode){ isTalking=!isTalking; ZelloClickService.clickZelloPTT(isTalking) }
                        else { ZelloClickService.clickZelloPTT(true); Handler(Looper.getMainLooper()).postDelayed({ZelloClickService.clickZelloPTT(false)}, 200) }
                        btn.alpha = if(isTalking && Prefs.lockMode) 1.0f else Prefs.alpha
                    }
                    true
                }
                else -> false
            }
        }
        wm.addView(view, params)
        startForeground(1, NotificationCompat.Builder(this,"float").setContentTitle("Palm Float Zello กำลังทำงาน").setContentText("กดได้ไม่จำกัด").setSmallIcon(android.R.drawable.ic_btn_speak_now).build())
    }
    override fun onDestroy(){ super.onDestroy(); try{ wm.removeView(view) }catch(e:Exception){} }
    override fun onBind(i: Intent?): IBinder? = null
}
