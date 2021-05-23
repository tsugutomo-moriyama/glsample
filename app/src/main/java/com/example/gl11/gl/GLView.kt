package com.example.gl11.gl

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.example.gl11.system.RotationSensor
import java.util.*

class GLView(
    context: Context, rotationSensor: RotationSensor,
    onSelectUnitListener:GLRenderer.OnSelectUnitListener) : GLSurfaceView(context) {

    private var distanceCurrent = 0f
    private var distanceStart = 0f
    private var timeStart: Long = 0
    private val scaleListener = object : ScaleGestureDetector.OnScaleGestureListener{
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            detector?.let{
                distanceCurrent = it.currentSpan
                if(it.eventTime > timeStart){
                    renderer?.let{r->
                        val s = r.scaleRange + (distanceCurrent - distanceStart)
                        if(s < 5000f && s > 50f){
                            r.scaleRange = s
                            distanceStart = distanceCurrent
                        }
                    }
                }
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            detector?.let{
                distanceStart = it.currentSpan
                timeStart = it.eventTime
            }
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {}
    }

    private var renderer: GLRenderer? = null
    private val scaleGestureDetector: ScaleGestureDetector
    private var environmentTimer: Timer? = null

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setBackgroundColor(Color.TRANSPARENT)

        renderer = GLRenderer(context).also {
            it.rotationSensor = rotationSensor
            it.onSelectUnitListener = onSelectUnitListener
        }
        setRenderer(renderer)

        scaleGestureDetector = ScaleGestureDetector(this.context, scaleListener)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(event.action == MotionEvent.ACTION_DOWN){
            renderer?.onTouchEvent(event.x, event.y)
        }
        return scaleGestureDetector.onTouchEvent(event)
    }

    override fun onPause() {
        super.onPause()
        environmentTimer?.cancel()
        environmentTimer = null
    }

    fun updateList(positionList: MutableList<PositionUnit>){
        renderer?.setPositionList(positionList)
    }
}