package com.palm.floatzello
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var btnToggle: Button
    private lateinit var seekSize: SeekBar
    private lateinit var seekAlpha: SeekBar
    private lateinit var switchLock: Switch
    private var isOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnToggle = findViewById(R.id.btnToggle)
        seekSize = findViewById(R.id.seekSize)
        seekAlpha = findViewById(R.id.seekAlpha)
        switchLock = findViewById(R.id.switchLock)

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.btnZello).setOnClickListener {
            try { startActivity(packageManager.getLaunchIntentForPackage("com.loudtalks")!!) }
            catch(e: Exception){ Toast.makeText(this,"ยังไม่ได้ติดตั้ง Zello",Toast.LENGTH_SHORT).show() }
        }
        seekSize.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean){ Prefs.size = 60 + p }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        seekAlpha.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean){ Prefs.alpha = 0.4f + (p/100f*0.6f) }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        switchLock.setOnCheckedChangeListener{_,b-> Prefs.lockMode = b }

        btnToggle.setOnClickListener {
            if(!Settings.canDrawOverlays(this)){
                Toast.makeText(this,"กดปุ่ม เปิดสิทธิ์แสดงทับแอพอื่น ก่อน",Toast.LENGTH_LONG).show(); return@setOnClickListener
            }
            isOn = !isOn
            if(isOn){
                startService(Intent(this, FloatingService::class.java))
                btnToggle.text = "ปิดปุ่มลอย - Turn OFF"
                Toast.makeText(this,"เปิดแล้ว! ไปเปิด Zello แล้วตั้งเป็น Solo ได้เลย",Toast.LENGTH_LONG).show()
            } else {
                stopService(Intent(this, FloatingService::class.java))
                btnToggle.text = "เปิดปุ่มลอย - Turn ON"
            }
        }
    }
}
object Prefs { var size = 110; var alpha = 1.0f; var lockMode = false }
