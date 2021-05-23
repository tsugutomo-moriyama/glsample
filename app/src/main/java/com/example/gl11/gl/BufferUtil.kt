package com.example.gl11.gl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object BufferUtil {
    fun makeFloatBuffer(array: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(array.size * 4).order(
            ByteOrder.nativeOrder()
        ).asFloatBuffer().also{
            it.put(array).position(0)
        }
    }

    fun makeByteBuffer(array: ByteArray): ByteBuffer {
        return ByteBuffer.allocateDirect(array.size).order(
            ByteOrder.nativeOrder()
        ).also{
          it.put(array).position(0)
        }
    }

    fun makeShortBuffer(array: ShortArray): ShortBuffer {
        return ByteBuffer.allocateDirect(array.size * 2).order(
            ByteOrder.nativeOrder()
        ).asShortBuffer().also{
            it.put(array).position(0)
        }
    }
}