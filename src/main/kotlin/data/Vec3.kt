package data

import Vec3G
import kotlin.math.pow
import kotlin.math.sqrt

data class Vec3(val x: Double, val y: Double, val z: Double) {
    val xy = Vec2(x, y)
    val yx = Vec2(y, x)
    val xz = Vec2(x, z)
    val zx = Vec2(z, x)
    val yz = Vec2(y, z)
    val zy = Vec2(z, y)

    companion object {
        val i = Vec3(1.0, 0.0, 0.0)
        val j = Vec3(0.0, 1.0, 0.0)
        val k = Vec3(0.0, 0.0, 1.0)
    }


    inline fun <T> forEachG(func: (Double) -> T): Vec3G<T> {
        return Vec3G<T>(func(x), func(y), func(z))
    }

    inline fun forEach(func: (Double) -> Double): Vec3 = Vec3(func(x), func(y), func(z))

    inline infix fun <T> substitute(func: (Double, Double, Double) -> T): T {
        return func(x, y, z)
    }


    operator fun div(a: Double): Vec3 = forEach { it / a }

    operator fun times(a: Double): Vec3 = forEach { it * a }

    operator fun times(rhs: Vec3): Vec3 = Vec3(x * rhs.x, y * rhs.y, z * rhs.z)

    operator fun minus(rhs: Vec3): Vec3 = Vec3(x - rhs.x, y - rhs.y, z - rhs.z)

    operator fun plus(rhs: Vec3): Vec3 {
        return Vec3(x + rhs.x, y + rhs.y, z + rhs.z)
    }

    operator fun unaryMinus(): Vec3 = forEach { -it }

    operator fun unaryPlus(): Vec3 {
        return Vec3(x, y, z)
    }

    fun pow(n: Double): Double {
        return x.pow(n) + y.pow(n) + z.pow(n)
    }

    fun norm(): Double {
        return sqrt(this dot this)
    }

    infix fun to(rhs: Vec3): Double {
        return (this - rhs).norm()
    }

    infix fun dot(rhs: Vec3): Double {
        return x * rhs.x + y * rhs.y + z * rhs.z
    }

    infix fun cross(rhs: Vec3): Vec3 {
        return Vec3(
            y * rhs.z - z * rhs.y,
            x * rhs.z - z * rhs.x,
            x * rhs.y - y * rhs.x
        )
    }

    fun unit(): Vec3 = this / this.norm()

    infix fun X(rhs: Vec3): Vec3 {
        return this cross rhs
    }

    fun reflect(a: Vec3): Vec3 {
        val I = a.unit()
        val N = unit()
        return I - N * 2.0 * (N dot I)
    }
}

