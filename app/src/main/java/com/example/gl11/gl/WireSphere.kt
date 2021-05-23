package com.example.gl11.gl

import android.opengl.GLES20
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

class WireSphere(nSlices: Int = 20, nStacks: Int = 10) {
    private var vertexBuffer: FloatBuffer? = null
    private var indexBuffer: ShortBuffer? = null
    private var nIndexes = 0

    init{
        makeSphere(nSlices, nStacks)
    }

    private fun makeSphere(nSlices: Int, nStacks: Int) {
        //頂点座標
        val radius = 1f
        val sizeArray = ((nStacks - 1) * nSlices + 2) * 3
        val vertexes = FloatArray(sizeArray)
        vertexes[0] = 0f
        vertexes[1] = radius
        vertexes[2] = 0f
        var px: Int
        var theta: Float
        var phi: Float
        var i = 0
        while (i < nStacks - 1) {
            var j = 1
            while (j <= nSlices) {
                px = (i * nSlices + j) * 3
                theta =
                    (nStacks - i - 1).toFloat() / nStacks.toFloat() * 3.14159265f - 3.14159265f * 0.5f
                phi = j.toFloat() / nSlices.toFloat() * 2f * 3.14159265f
                vertexes[px] =
                    (radius * cos(theta.toDouble()) * sin(phi.toDouble())).toFloat()
                vertexes[px + 1] = (radius * sin(theta.toDouble())).toFloat()
                vertexes[px + 2] =
                    (radius * cos(theta.toDouble()) * cos(phi.toDouble())).toFloat()
                j++
            }
            i++
        }
        px = ((nStacks - 1) * nSlices + 1) * 3
        vertexes[px] = 0f
        vertexes[px + 1] = -radius
        vertexes[px + 2] = 0f

        nIndexes = (nStacks * nSlices + nSlices * (nStacks - 1)) * 2
        val indexes = ShortArray(nIndexes)
        var p = 0
        i = 0
        while (i < nSlices) {
            var j = 0
            while (j < nStacks) {
                when (j) {
                    0 -> {
                        indexes[p++] = 0
                        indexes[p++] = (i + 1).toShort()
                        indexes[p++] = (i + 1).toShort()
                        indexes[p++] = ((i + 1) % nSlices + 1).toShort()
                    }
                    nStacks - 1 -> {
                        indexes[p++] = ((j - 1) * nSlices + i + 1).toShort()
                        indexes[p++] = ((nStacks - 1) * nSlices + 1).toShort()
                    }
                    else -> {
                        indexes[p++] = ((j - 1) * nSlices + i + 1).toShort()
                        indexes[p++] = (j * nSlices + i + 1).toShort()
                        indexes[p++] = (j * nSlices + i + 1).toShort()
                        indexes[p++] = (j * nSlices + (i + 1) % nSlices + 1).toShort()
                    }
                }
                j++
            }
            i++
        }
        vertexBuffer = BufferUtil.makeFloatBuffer(vertexes)
        indexBuffer = BufferUtil.makeShortBuffer(indexes)
    }

    fun draw(util:GLUtil, r: Float, g: Float, b: Float, a: Float) {
        GLES20.glVertexAttribPointer(util.positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glUniform4f(util.objectColorHandle, r, g, b, a)

        indexBuffer?.position(0)
        GLES20.glDrawElements(GLES20.GL_LINES, nIndexes, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
    }
}