import kotlin.math.*

const val Epsilon: Double = 1e-12

fun Double.truncate(min: Double, max: Double): Double {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

fun Int.truncate(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

data class Vec2(val x: Double, val y: Double){
    fun rotate(theta:Double, ):Vec2 = Vec2(
        x * cos(theta) - y * sin(theta),
        x * sin(theta) + y * cos(theta)
    )
}

fun ran(i:Int,n:Int): Vec2 {
    //if (i == 0) return Vec2(0.0, 0.0)
    val sample = 10000
    val theta = ((0..sample).random().toDouble() / sample + i) * (2 * PI / n)
    val r = (-sample..sample).random().toDouble() / sample / 2
    return Vec2(r * cos(theta), r * sin(theta))
}

data class Vec3G<T>(val x: T, val y: T, val z: T)

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

    inline infix fun <T> substitute(func:(Double,Double,Double)->T):T{
        return func(x,y,z)
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

data class Quaternion(val s:Vec3, val omega:Double) {
    operator fun times(rhs: Quaternion): Quaternion {
        return Quaternion(
            (s X rhs.s) + omega * rhs.s + rhs.omega * s,
            omega * rhs.omega - (s dot rhs.s)
        )
    }

    operator fun div(rhs: Double): Quaternion {
        return Quaternion(s / rhs, omega / rhs)
    }

    fun norm(): Double {
        return s.norm() + omega.pow(2.0)
    }

    fun invert(): Quaternion {
        return Quaternion(-s, omega) / norm()
    }
}

operator fun Double.minus(a: Vec3) = Vec3(this - a.x, this - a.y, this - a.z)
operator fun Double.plus(a: Vec3) = Vec3(this + a.x, this + a.y, this + a.z)
operator fun Double.times(a: Vec3) = Vec3(this * a.x, this * a.y, this * a.z)

class fixedVec3(var position: Vec3, var direction: Vec3){

}