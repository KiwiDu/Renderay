import data.Vec2
import data.Vec3
import kotlin.math.*

const val Epsilon: Double = 1e-12

fun Double.truncate(min: Double, max: Double) = when {
    this < min -> min
    this > max -> max
    else -> this
}


fun Int.truncate(min: Int, max: Int) = when {
    this < min -> min
    this > max -> max
    else -> this
}

operator fun Double.minus(a: Vec3) = Vec3(this - a.x, this - a.y, this - a.z)
operator fun Double.plus(a: Vec3) = Vec3(this + a.x, this + a.y, this + a.z)
operator fun Double.times(a: Vec3) = Vec3(this * a.x, this * a.y, this * a.z)

fun ran(i: Int, n: Int): Vec2 {
    //if (i == 0) return data.Vec2(0.0, 0.0)
    val sample = 10000
    val theta = ((0..sample).random().toDouble() / sample + i) * (2 * PI / n)
    val r = (-sample..sample).random().toDouble() / sample / 3
    return Vec2(r * cos(theta), r * sin(theta))
}

data class Vec3G<T>(val x: T, val y: T, val z: T)