package data

import times
import kotlin.math.pow

data class Quaternion(val s: Vec3, val omega:Double) {
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