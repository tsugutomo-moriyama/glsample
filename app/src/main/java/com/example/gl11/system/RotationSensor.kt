package com.example.gl11.system

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import kotlin.math.sqrt

class RotationSensor(
    private val sensorManager:SensorManager,
    private val rotation:Int?) {

    val matrix: FloatArray
        get() = rotMatrix.clone()

    private val axisMap = mapOf(
        Surface.ROTATION_0 to Pair(SensorManager.AXIS_X, SensorManager.AXIS_Y),
        Surface.ROTATION_90 to Pair(SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X),
        Surface.ROTATION_180 to Pair(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y),
        Surface.ROTATION_270 to Pair(SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X),
    )
    private var sensorEventListener: SensorEventListener? = null
    private var sensorMode = 0
    private var isReady = 0 //センサ値の準備状況
    private var firstTime = true
    private var rotMatrix = FloatArray(16)
    private var tmpRotMatrix = FloatArray(16)
    private var iMatrix = FloatArray(16)
    private var fGyroscope = FloatArray(3)
    private var fMagneticValues = FloatArray(3)
    private var fAccelerometerValues = FloatArray(3)
    private var errorCount = 0
    private var noErrorCount = 0
    private var xAxis = 0
    private var yAxis = 0
    private var accPr = floatArrayOf(0f, 0f, 0f)
    private var magPr = floatArrayOf(0f, 0f, 0f)
    private var fltValue = 0.5f
    private var accNPr = 0.0
    private var magNPr = 0.0

    fun setSensorsOn(): Int {
        firstTime = true
        isReady = 0
        sensorMode = 0
        Matrix.setIdentityM(rotMatrix, 0)
        Matrix.setIdentityM(tmpRotMatrix, 0)

        axisMap[rotation]?.let{
            xAxis = it.first
            yAxis = it.second
        }

        var sensorAvailable = 0
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let{sensor->
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI)
            sensorAvailable = sensorAvailable or 2
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let{ sensor->
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI)
            sensorAvailable = sensorAvailable or 1
        }
        if (sensorAvailable and 3 == 3) {
            sensorMode = 1
            return sensorMode
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let{ sensor->
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI)
            sensorAvailable = sensorAvailable or 4
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let{ sensor->
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI )
            sensorAvailable = sensorAvailable or 8
        }
        if (sensorAvailable and 0xc == 0xc) {
            sensorMode = 2
            return sensorMode
        }
        return sensorMode
    }

    fun setSensorsOff() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    private fun getNorm(vec: FloatArray): Float {
        val v2 = (vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2]).toDouble()
        return sqrt(v2).toFloat()
    }

    private fun getMovement(acc: FloatArray, mag: FloatArray): Float {
        val accD = FloatArray(4)
        val magD = FloatArray(4)
        for (i in 0..2) {
            accD[i] = acc[i] - accPr[i]
            accPr[i] = acc[i]
            magD[i] = mag[i] - magPr[i]
            magPr[i] = mag[i]
        }
        val acc2: Double = (accD[0] * accD[0] + accD[1] * accD[1] + accD[2] * accD[2]).toDouble()
        val accN: Double
        if (acc2 < 1e-4) {
            accN = accNPr
        } else {
            accN = sqrt(acc2)
            accNPr = accN
        }
        val mag2: Double = (magD[0] * magD[0] + magD[1] * magD[1] + magD[2] * magD[2]).toDouble() / 5
        val magN: Double
        if (mag2 < 1e-4) {
            magN = magNPr
        } else {
            magN = sqrt(mag2)
            magNPr = magN
        }
        Log.i(TAG, "accn, magn = $accN, $magN")
        var ret: Float = (accN + magN).toFloat()
        ret = .7f * fltValue + 0.3f * ret
        fltValue = ret
        return ret
    }

    private fun checkOrthogonalMatrix(mat: FloatArray): Float {
        val tmpMatrix = FloatArray(16)
        val multiMatrix = FloatArray(16)
        Matrix.transposeM(tmpMatrix, 0, mat, 0)
        Matrix.multiplyMM(multiMatrix, 0, mat, 0, tmpMatrix, 0)
        val im = FloatArray(16)
        Matrix.setIdentityM(im, 0)
        var ret = 0f
        for (i in 0..15) {
            val elm = multiMatrix[i] - im[i]
            ret += elm * elm
        }
        return ret
    }

    private fun changeSensor() {
        setSensorsOff()
        firstTime = true
        isReady = 0
        sensorMode = 0
        Matrix.setIdentityM(rotMatrix, 0)
        Matrix.setIdentityM(tmpRotMatrix, 0)
        var sensoravailable = 0

        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let{ sensor->
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI)
            sensoravailable = sensoravailable or 4
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let{ sensor->
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI)
            sensoravailable = sensoravailable or 8
        }
        if (sensoravailable and 0xc == 0xc) {
            sensorMode = 2
        }
    }

    init {
        sensorEventListener = object : SensorEventListener {
            var rMat = FloatArray(16)
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rMat, event.values)
                        SensorManager.remapCoordinateSystem(rMat, xAxis, yAxis, tmpRotMatrix)
                        if (noErrorCount < 200) {
                            val errorValue = checkOrthogonalMatrix(tmpRotMatrix)
                            Log.i(TAG, "ROTATION_VECTOR orthogonal check = $errorValue")
                            if (0.1f < errorValue) {
                                errorCount++
                                if (50 < errorCount) changeSensor()
                            } else {
                                noErrorCount++
                            }
                        }
                        isReady = isReady or 1
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        fGyroscope = event.values.clone()
                        isReady = isReady or 2
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        fMagneticValues = event.values.clone()
                        isReady = isReady or 4
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        fAccelerometerValues = event.values.clone()
                        isReady = isReady or 8
                    }
                }
                if (firstTime) {
                    if (sensorMode == 1) {
                        if (isReady and 1 == 1) {
                            for (i in 0..2) {
                                for (j in 0..2) {
                                    rotMatrix[4 * i + j] = tmpRotMatrix[4 * i + j]
                                }
                            }
                            firstTime = false
                        }
                    } else {
                        if (isReady and 0xc == 0xc) {
                            SensorManager.getRotationMatrix(rMat, iMatrix, fAccelerometerValues, fMagneticValues)
                            SensorManager.remapCoordinateSystem(rMat, xAxis, yAxis, rotMatrix)
                            getMovement(fAccelerometerValues, fMagneticValues)
                            firstTime = false
                        }
                    }
                } else {
                    if (sensorMode == 1) {
                        if (isReady and 3 == 3) {
                            var gyr = getNorm(fGyroscope)
                            if (1f < gyr) gyr = 1f else if (gyr < 0.01f) gyr = .01f
                            val weight = 1f - gyr
                            for (i in 0..2) {
                                for (j in 0..2) {
                                    rotMatrix[4 * i + j] =
                                        weight * rotMatrix[4 * i + j] + gyr * tmpRotMatrix[4 * i + j]
                                }
                            }
                        }
                    } else if (sensorMode == 2) {
                        var weight1 = getMovement(fAccelerometerValues, fMagneticValues) / 30f
                        if (1f < weight1) weight1 = 1f else if (weight1 < 0.001f) weight1 = 0.001f
                        val weight0 = 1f - weight1
                        if (isReady and 0xc == 0xc) {
                            SensorManager.getRotationMatrix(rMat, iMatrix, fAccelerometerValues, fMagneticValues)
                            SensorManager.remapCoordinateSystem(rMat, xAxis, yAxis, tmpRotMatrix)
                            for (i in 0..2) {
                                for (j in 0..2) {
                                    rotMatrix[4 * i + j] =
                                        weight0 * rotMatrix[4 * i + j] + weight1 * tmpRotMatrix[4 * i + j]
                                }
                            }
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }
    companion object{
        private const val TAG ="RotationSensor"
    }
}