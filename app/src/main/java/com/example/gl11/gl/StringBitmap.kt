package com.example.gl11.gl

import android.graphics.*
import kotlin.math.abs

class StringBitmap {
    private class TextSize(p:Paint, val text:String, textSize: Float){
        val fm:Paint.FontMetrics = p.also{
            it.textSize = textSize
        }.fontMetrics

        var w = 0
        var h = 0
        init{
            w = p.measureText(text).toInt()
            h = (abs(fm.top) + fm.bottom).toInt()
            if (w == 0) w = 10
            if (h == 0) h = 10
        }
        fun getBitmapSize():Int{
            var bitmapSize = 2
            while (bitmapSize < w) bitmapSize *= 2
            while (bitmapSize < h) bitmapSize *= 2
            return bitmapSize
        }
    }
    companion object {
        private const val FONT_SIZE1 = 40f
        private const val FONT_SIZE2 = 30f
        private const val FONT_SIZE3 = 20f
        private const val FONT_SIZE4 = 10f
        private val BK_COLOR = Color.parseColor("#A0000000")
        private val SELECT_COLOR = Color.parseColor("#C0D0D0D0")

        fun createBitmap(unit: PositionUnit, frame: Boolean = true): Bitmap {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                strokeWidth = 0F
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            var ts = TextSize(paint, unit.text, FONT_SIZE1)
            val bitmapSize = if (!frame) {
                ts.getBitmapSize()
            } else {
                256
            }
            if(bitmapSize < ts.w) {
                ts = TextSize(paint, unit.text, FONT_SIZE2)
                if(bitmapSize < ts.w) {
                    ts = TextSize(paint, unit.text, FONT_SIZE3)
                    if(bitmapSize < ts.w) {
                        ts = TextSize(paint, unit.text, FONT_SIZE4)
                    }

                }
            }
            val bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            paint.color = if(unit.isSelected){ SELECT_COLOR }else{ BK_COLOR }
            val rect = Rect(5, 5, bitmapSize - 5, bitmapSize - 5)
            canvas.drawRect(rect, paint)
            setPaintColor(paint, unit.colors, bitmapSize.toFloat())

            if (frame) {
                canvas.drawRect(rect, paint.apply { style = Paint.Style.STROKE })
            }
            canvas.drawText(
                ts.text,
                (bitmapSize / 2 - ts.w / 2).toFloat(),
                bitmapSize / 2 - (ts.fm.ascent + ts.fm.descent) / 2,
                paint.apply { style = Paint.Style.FILL })
            return bitmap
        }

        fun createBitmap(
            text:String,
            frame: Boolean = true,
            colors:List<Int> = listOf(Color.WHITE),
            bkColor:Int = BK_COLOR
        ): Bitmap {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                strokeWidth = 0F
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            var ts = TextSize(paint, text, FONT_SIZE1)
            val bitmapSize = if (!frame) {
                ts.getBitmapSize()
            } else {
                256
            }
            if(bitmapSize < ts.w) {
                ts = TextSize(paint, text, FONT_SIZE2)
                if(bitmapSize < ts.w) {
                    ts = TextSize(paint, text, FONT_SIZE3)
                    if(bitmapSize < ts.w) {
                        ts = TextSize(paint, text, FONT_SIZE4)
                    }

                }
            }
            val bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            paint.color = bkColor
            val rect = Rect(5, 5, bitmapSize - 5, bitmapSize - 5)
            canvas.drawRect(rect, paint)

            setPaintColor(paint, colors, bitmapSize.toFloat())
            if (frame) {
                canvas.drawRect(rect, paint.apply { style = Paint.Style.STROKE })
            }
            canvas.drawText(
                ts.text,
                (bitmapSize / 2 - ts.w / 2).toFloat(),
                bitmapSize / 2 - (ts.fm.ascent + ts.fm.descent) / 2,
                paint.apply { style = Paint.Style.FILL })
            return bitmap
        }
        private fun setPaintColor(paint: Paint, colors:List<Int>, bitmapSize:Float){
            when (colors.size) {
                0 -> {
                    paint.color = Color.WHITE
                }
                1 -> {
                    paint.color = colors.first()
                }
                else -> {
                    paint.shader = LinearGradient(
                        0f, 0f, bitmapSize, bitmapSize,
                        colors.toIntArray(), null, Shader.TileMode.CLAMP
                    )
                }
            }
        }
    }
}