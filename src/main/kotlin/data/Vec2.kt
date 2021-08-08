package data

import kotlin.math.cos
import kotlin.math.sin

data class Vec2(val x: Double, val y: Double){
    fun rotate(theta:Double, ): Vec2 = Vec2(
        x * cos(theta) - y * sin(theta),
        x * sin(theta) + y * cos(theta)
    )
}