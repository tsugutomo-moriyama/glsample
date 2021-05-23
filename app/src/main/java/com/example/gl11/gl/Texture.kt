package com.example.gl11.gl

import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLUtils
import javax.microedition.khronos.opengles.GL10


class Texture() {

    private var textureId = -1
    private var textureUnitNumber = 0
    fun makeTexture(bitmap: Bitmap){
        val firstIndex = 0
        val defaultOffset = 0
        val textures = IntArray(1)
        if (textureId != -1) {
            textures[firstIndex] = textureId
            GLES20.glDeleteTextures(1, textures, defaultOffset)
        }
        GLES20.glGenTextures(1, textures, defaultOffset)
        textureId = textures[firstIndex]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)

        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
    }
    fun makeTexture(text: String, frame:Boolean = true, colors:List<Int>, bkColor:Int) {
        val bmp0 = StringBitmap.createBitmap(text,frame, colors, bkColor)
        makeTexture(bmp0)
        bmp0.recycle()
    }

    fun makeTexture(positionUnit: PositionUnit) {
        val bmp1 = if(positionUnit.isSelected){null} else{positionUnit.bitmap}
        if(bmp1 == null){
            val bmp0 = StringBitmap.createBitmap(positionUnit)
            makeTexture(bmp0)
            bmp0.recycle()
        } else {
            makeTexture(bmp1)
        }
    }

    fun setTexture(util:GLUtil) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureUnitNumber)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(util.textureHandle, textureUnitNumber) //テクスチャユニット番号を指定する
    }
}