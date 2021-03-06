import data.Vec3
import kotlin.math.abs

interface Shape {
    companion object {
        fun union(vararg s: Shape): Shape =
            object : Shape {
                override fun getSDF(point: Vec3): Double =
                    s.minOf { it.getSDF(point) }
            }
        fun intersect(vararg s: Shape): Shape =
            object : Shape {
                override fun getSDF(point: Vec3): Double =
                    s.maxOf { it.getSDF(point) }
            }
    }
    fun getSDF(point: Vec3): Double
    fun getNormal(point: Vec3): Vec3 {
        val delta = 1e-6
        val dx = delta * Vec3.i
        val dy = delta * Vec3.j
        val dz = delta * Vec3.k
        val sd = getSDF(point)//signed distance
        return Vec3(
            (getSDF(dx + point) - sd),
            (getSDF(dy + point) - sd),
            (getSDF(dz + point) - sd),
        ).unit()
    }
}

class Ball(private val center: Vec3, private val radius: Double) : Shape {
    override fun getSDF(point: Vec3): Double {
        return (center to point) - radius
    }

    override fun getNormal(point: Vec3): Vec3 {
        return (point - center).unit()
    }
}

class Plane(normal: Vec3, private val pass: Vec3) : Shape {
    private val normalUnit = normal.unit()
    override fun getSDF(point: Vec3): Double {
        return (point - pass) dot normalUnit
    }

    override fun getNormal(point: Vec3): Vec3 {
        return normalUnit
    }
}

class Capsule(private val a: Vec3, private val b: Vec3, private val radius: Double) : Shape {
    private val ab = b - a
    fun nearestPoint(point: Vec3): Vec3 {
        val ap = point - a
        val l = ((ap dot ab) / ab.norm()).truncate(0.0, ab.norm())
        return a + l * ab.unit()
    }

    override fun getSDF(point: Vec3): Double {
        return (nearestPoint(point) to point) - radius
    }

    override fun getNormal(point: Vec3): Vec3 {
        return (point - nearestPoint(point)).unit()
    }
}

class OrthoBox(private val position: Vec3, private val diag: Vec3, insideOut:Boolean=false):Shape {
    val k: Double = if (insideOut) -1.0 else 1.0
    override fun getSDF(point: Vec3): Double {
        val pos = point - position
        val d = pos.forEach(::abs) - diag.forEach(::abs)
        val (dx, dy, dz) = d
        return k * if (dx <= 0 && dy <= 0 && dz <= 0) maxOf(dx, dy, dz)
        else d.forEach { if (it > 0) it else 0.0 }.norm()
    }

}