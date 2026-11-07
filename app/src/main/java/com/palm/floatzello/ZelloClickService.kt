package com.palm.floatzello
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ZelloClickService : AccessibilityService() {
    companion object {
        var instance: ZelloClickService? = null
        fun clickZelloPTT(down: Boolean){
            val svc = instance ?: return
            val root = svc.rootInActiveWindow ?: return
            val candidates = listOf("TALK","พูด","Hold to talk","Tap to talk","PTT")
            for(key in candidates){
                val nodes = root.findAccessibilityNodeInfosByText(key)
                for(n in nodes){ if(n.isClickable){ n.performAction(AccessibilityNodeInfo.ACTION_CLICK); return } }
            }
        }
    }
    override fun onServiceConnected(){ instance=this }
    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt(){}
    override fun onDestroy(){ instance=null; super.onDestroy() }
}
