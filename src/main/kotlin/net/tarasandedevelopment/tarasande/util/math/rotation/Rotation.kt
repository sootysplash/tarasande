package net.tarasandedevelopment.tarasande.util.math.rotation

import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.tarasandedevelopment.tarasande.system.base.valuesystem.impl.ValueNumberRange
import net.tarasandedevelopment.tarasande.util.extension.mc
import net.tarasandedevelopment.tarasande.util.extension.minecraft.times
import net.tarasandedevelopment.tarasande.util.render.RenderUtil
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.round
import kotlin.math.sqrt

class Rotation(var yaw: Float, var pitch: Float) {

    constructor(other: Rotation) : this(other.yaw, other.pitch)
    constructor(entity: Entity) : this(entity.yaw, entity.pitch)

    companion object {
        const val MAXIMUM_DELTA = 255.0 // sqrt(180 * 180 + 180 * 180)

        fun getGcd(): Double {
            val sensitivity = mc.options.mouseSensitivity.value * 0.6F.toDouble() + 0.2F.toDouble()
            val sensitivityPow3 = sensitivity * sensitivity * sensitivity
            val sensitivityPow3Mult8 = sensitivityPow3 * 8.0

            return if (mc.options.perspective.isFirstPerson && mc.player!!.isUsingSpyglass)
                sensitivityPow3
            else
                sensitivityPow3Mult8
        }

        fun calculateRotationChange(cursorDeltaX: Double, cursorDeltaY: Double): Rotation {
            val gcd = getGcd()
            return Rotation((-cursorDeltaX * gcd).toFloat() * 0.15F, (-cursorDeltaY * gcd).toFloat() * 0.15F)
        }

        fun approximateCursorDeltas(deltaRotation: Rotation): Pair<Double, Double> {
            val gcd = getGcd() * 0.15F
            return Pair(-round(deltaRotation.yaw / gcd), -round(deltaRotation.pitch / gcd))
        }
    }

    // I can't define prevRotation in the arguments because java wants to call it too

    fun correctSensitivity(): Rotation {
        return correctSensitivity(RotationUtil.fakeRotation ?: Rotation(mc.player!!))
    }

    fun correctSensitivity(prevRotation: Rotation): Rotation {
        val deltaRotation = closestDelta(prevRotation)

        val cursorDeltas = approximateCursorDeltas(deltaRotation)
        val rotationChange = calculateRotationChange(cursorDeltas.first, cursorDeltas.second)

        val delta = prevRotation - rotationChange

        yaw = delta.yaw
        pitch = delta.pitch.coerceIn(-90.0F, 90.0F)

        return this
    }

    fun smoothedTurn(target: Rotation, aimSpeed: ValueNumberRange): Rotation {
        return smoothedTurn(target, Pair(aimSpeed.minValue, aimSpeed.maxValue))
    }

    fun smoothedTurn(target: Rotation, aimSpeed: Pair<Double, Double>): Rotation {
        val smoothness =
            if (aimSpeed.first == 1.0 && aimSpeed.second == 1.0)
                1.0
            else {
                val randomAimSpeed =
                    if (aimSpeed.first == aimSpeed.second)
                        aimSpeed.first
                    else
                        ThreadLocalRandom.current().nextDouble(aimSpeed.first, aimSpeed.second)

                (randomAimSpeed * RenderUtil.deltaTime * 0.05).coerceIn(0.0..1.0)
            }
        return smoothedTurn(target, smoothness)
    }

    fun smoothedTurn(target: Rotation, smoothness: Double): Rotation {
        val deltaRotation = closestDelta(target) * smoothness.toFloat()
        yaw += deltaRotation.yaw
        pitch += deltaRotation.pitch
        return this
    }

    fun fov(other: Rotation): Float {
        return dist(closestDelta(other))
    }

    fun closestDelta(other: Rotation): Rotation {
        return Rotation(MathHelper.wrapDegrees(other.yaw - yaw), other.pitch - pitch)
    }

    private fun dist(deltaRotation: Rotation): Float {
        return sqrt(deltaRotation.yaw * deltaRotation.yaw + deltaRotation.pitch * deltaRotation.pitch)
    }

    override fun toString(): String {
        return "Rotation(yaw=$yaw, pitch=$pitch)"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Rotation) return false

        if (yaw != other.yaw) return false
        if (pitch != other.pitch) return false

        return true
    }

    override fun hashCode(): Int {
        var result = yaw.hashCode()
        result = 31 * result + pitch.hashCode()
        return result
    }

    fun forwardVector(dist: Double): Vec3d {
        val f = Math.toRadians(pitch.toDouble()).toFloat()
        val g = Math.toRadians(-yaw.toDouble()).toFloat()
        val h = MathHelper.cos(g)
        val i = MathHelper.sin(g)
        val j = MathHelper.cos(f)
        val k = MathHelper.sin(f)
        return Vec3d((i * j).toDouble(), -k.toDouble(), (h * j).toDouble()) * dist
    }

    operator fun plus(other: Rotation) = Rotation(yaw + other.yaw, pitch + other.pitch)
    operator fun minus(other: Rotation) = Rotation(yaw - other.yaw, pitch - other.pitch)
    operator fun times(value: Float) = Rotation(yaw * value, pitch * value)

}