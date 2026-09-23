package com.example.engine3d

import kotlin.math.cos
import kotlin.math.sin

class Camera3D(
    var target: Vector3 = Vector3(0f, 1.2f, 0f),
    var distance: Float = 6.5f,
    var yaw: Float = 0f, // in radians
    var pitch: Float = 0.38f // in radians (looking slightly down)
) {
    var eye: Vector3 = Vector3(0f, 3f, -6f)
        private set

    fun update(targetPosition: Vector3, dt: Float = 0.016f) {
        // Smoothly follow target position (Capitão Angola's chest height)
        val desiredTarget = targetPosition + Vector3(0f, 1.2f, 0f)
        target = Vector3.lerp(target, desiredTarget, 8f * dt)

        val clampedPitch = pitch.coerceIn(0.1f, 1.2f)
        val hDist = distance * cos(clampedPitch)
        val vDist = distance * sin(clampedPitch)

        val desiredEye = Vector3(
            target.x - hDist * sin(yaw),
            target.y + vDist,
            target.z - hDist * cos(yaw)
        )

        eye = Vector3.lerp(eye, desiredEye, 10f * dt)
    }

    fun getViewMatrix(): Matrix4 {
        return Matrix4.createLookAt(eye, target, Vector3.UP)
    }

    fun rotate(deltaYaw: Float, deltaPitch: Float) {
        yaw += deltaYaw
        pitch = (pitch + deltaPitch).coerceIn(0.12f, 1.15f)
    }
}
