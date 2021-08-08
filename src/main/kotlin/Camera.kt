import data.Quaternion
import data.Vec3
import kotlin.math.sqrt

class Camera(private val position: Vec3,
             direction: Vec3,
             private val ratio:Double,
             private val mode:Mode) {
    private val unitDirection = direction.unit()
    private val uAxis = Vec3(1.0, 0.0, 0.0).rotate3D()
    private val vAxis = Vec3(0.0, 1.0, 0.0).rotate3D()

    enum class Mode {
        Perspective, Parallel
    }

    fun emit(u: Double, v: Double): Ray {
        return when (mode) {
            Mode.Perspective -> Ray(position, unitDirection + fromUV(u, v))
            Mode.Parallel -> Ray(position + fromUV(u, v), unitDirection)
        }
    }

    private fun Vec3.rotate3D(): Vec3 {
        val axis = unitDirection X Vec3(0.0, 0.0, 1.0)
        val cosTheta = unitDirection.z// dot data.Vec3(0.0, 0.0, 1.0)
        val cosHalfTheta = sqrt((1.0 + cosTheta) / 2.0)
        val sinHalfTheta = sqrt((1.0 - cosTheta) / 2.0)
        val p = Quaternion(this, 0.0)
        val q = Quaternion(axis * sinHalfTheta, cosHalfTheta)
        val result = q * p * q.invert()
        return result.s
    }

    private fun fromUV(u: Double, v: Double): Vec3 {
        return (u * uAxis + v * vAxis) * ratio
    }
}