package com.example.engine3d

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Matrix4 {
    val data = FloatArray(16)

    init {
        identity()
    }

    fun identity(): Matrix4 {
        for (i in 0..15) data[i] = 0f
        data[0] = 1f
        data[5] = 1f
        data[10] = 1f
        data[15] = 1f
        return this
    }

    operator fun times(other: Matrix4): Matrix4 {
        val res = Matrix4()
        for (row in 0..3) {
            for (col in 0..3) {
                var sum = 0f
                for (k in 0..3) {
                    sum += this.data[row * 4 + k] * other.data[k * 4 + col]
                }
                res.data[row * 4 + col] = sum
            }
        }
        return res
    }

    fun transform(v: Vector3): Vector3 {
        val x = v.x * data[0] + v.y * data[1] + v.z * data[2] + data[3]
        val y = v.x * data[4] + v.y * data[5] + v.z * data[6] + data[7]
        val z = v.x * data[8] + v.y * data[9] + v.z * data[10] + data[11]
        val w = v.x * data[12] + v.y * data[13] + v.z * data[14] + data[15]
        return if (w != 0f && w != 1f) Vector3(x / w, y / w, z / w) else Vector3(x, y, z)
    }

    companion object {
        fun createTranslation(x: Float, y: Float, z: Float): Matrix4 {
            val m = Matrix4()
            m.data[3] = x
            m.data[7] = y
            m.data[11] = z
            return m
        }

        fun createScale(sx: Float, sy: Float, sz: Float): Matrix4 {
            val m = Matrix4()
            m.data[0] = sx
            m.data[5] = sy
            m.data[10] = sz
            return m
        }

        fun createRotationY(angleRad: Float): Matrix4 {
            val m = Matrix4()
            val c = cos(angleRad)
            val s = sin(angleRad)
            m.data[0] = c
            m.data[2] = s
            m.data[8] = -s
            m.data[10] = c
            return m
        }

        fun createRotationX(angleRad: Float): Matrix4 {
            val m = Matrix4()
            val c = cos(angleRad)
            val s = sin(angleRad)
            m.data[5] = c
            m.data[6] = -s
            m.data[9] = s
            m.data[10] = c
            return m
        }

        fun createPerspective(fovYDegrees: Float, aspect: Float, near: Float, far: Float): Matrix4 {
            val m = Matrix4()
            val fovRad = Math.toRadians(fovYDegrees.toDouble()).toFloat()
            val f = 1f / tan(fovRad / 2f)
            m.data[0] = f / aspect
            m.data[5] = f
            m.data[10] = (far + near) / (near - far)
            m.data[11] = (2f * far * near) / (near - far)
            m.data[14] = -1f
            m.data[15] = 0f
            return m
        }

        fun createLookAt(eye: Vector3, target: Vector3, up: Vector3): Matrix4 {
            val zAxis = (eye - target).normalized()
            val xAxis = up.cross(zAxis).normalized()
            val yAxis = zAxis.cross(xAxis).normalized()

            val m = Matrix4()
            m.data[0] = xAxis.x
            m.data[1] = xAxis.y
            m.data[2] = xAxis.z
            m.data[3] = -xAxis.dot(eye)

            m.data[4] = yAxis.x
            m.data[5] = yAxis.y
            m.data[6] = yAxis.z
            m.data[7] = -yAxis.dot(eye)

            m.data[8] = zAxis.x
            m.data[9] = zAxis.y
            m.data[10] = zAxis.z
            m.data[11] = -zAxis.dot(eye)

            return m
        }
    }
}
