package com.example.gl11.gl

import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import com.example.gl11.entity.DataType
import com.example.gl11.entity.Favorite
import com.example.gl11.entity.Position
import com.example.gl11.entity.Station

open class DisplayUnit(){
    // 表示範囲座標
    var tl:FloatArray = FloatArray(4)
    var tr:FloatArray = FloatArray(4)
    var bl:FloatArray = FloatArray(4)
    var br:FloatArray = FloatArray(4)

    fun isHit(x:Float, y:Float):Boolean{
        if(((tl[0] < -1f || tl[0] > 1f) && (tr[0] < -1f || tr[0] > 1f) &&
            (tr[0] < -1f || tr[0] > 1f) && (br[0] < -1f || br[0] > 1f)) ||
            ((tl[1] < -1f || tl[1] > 1f) && (tr[1] < -1f || tr[1] > 1f) &&
            (br[1] < -1f || br[1] > 1f) && (br[1] < -1f || br[1] > 1f))
        ){
            return false
        }
        val a: Float = calcExteriorProduct(tl, tr, x, y)
        val b: Float = calcExteriorProduct(tr, br, x, y)
        val c: Float = calcExteriorProduct(br, bl, x, y)
        val d: Float = calcExteriorProduct(bl, tl, x, y)
        return (a > 0 && b > 0 && c > 0 && d > 0)
    }

    private fun calcExteriorProduct(a: FloatArray, b: FloatArray, pX: Float, pY: Float): Float {
        return calcExteriorProduct(a[0], a[1]*-1, b[0], b[1]*-1, pX, pY*-1)
    }

    private fun calcExteriorProduct(aX: Float, aY: Float, bX: Float, bY: Float, pX: Float, pY: Float): Float {
        return (aX - bX) * (aY - pY) - (aX - pX) * (aY - bY)
    }
}