package com.example.gl11.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class TexRectangular(width: Float = 1f, height: Float = 1f) {
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ByteBuffer? = null
    private var normalBuffer: FloatBuffer? = null
    private var texCoordinateBuffer: FloatBuffer? = null

    private val indexes = byteArrayOf(0, 2, 1, 3)
    private val normals = floatArrayOf(
        0f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 1f,
        0f, 0f, 1f
    )
    private var coordinates = floatArrayOf(
        0f, 0f,
        1f, 0f,
        0f, 1f,
        1f, 1f
    )

    init {
        setRectangular(width, height)
    }

    private fun setRectangular(width: Float, height: Float) {
        val top = height * .5f
        val bottom = -top
        val right = width * .5f
        val left = -right

        //頂点座標
        val vertexs = floatArrayOf(
            left, top, 0f,  //左上 0
            right, top, 0f,  //右上 1
            left, bottom, 0f,  //左下 2
            right, bottom, 0f //右下 3
        )
        vertexBuffer = BufferUtil.makeFloatBuffer(vertexs)
        indexBuffer = BufferUtil.makeByteBuffer(indexes)
        normalBuffer = BufferUtil.makeFloatBuffer(normals)
        texCoordinateBuffer = BufferUtil.makeFloatBuffer(coordinates)
    }

    fun draw(util:GLUtil, r: Float, g: Float, b: Float, a: Float, shininess: Float) {
        GLES20.glVertexAttribPointer(util.texCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordinateBuffer)
        GLES20.glVertexAttribPointer(util.positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(util.normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)

        GLES20.glUniform4f(util.materialAmbientHandle, r, g, b, a)
        GLES20.glUniform4f(util.materialDiffuseHandle, r, g, b, a)
        GLES20.glUniform4f(util.materialSpecularHandle, 1f, 1f, 1f, a)
        GLES20.glUniform1f(util.materialShininessHandle, shininess)
        GLES20.glUniform4f(util.objectColorHandle, r, g, b, a)

        indexBuffer?.position(0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP,4, GLES20.GL_UNSIGNED_BYTE, indexBuffer)
    }
}