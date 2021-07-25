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
        val SDF = getSDF(point)
        return Vec3(
            (getSDF(dx + point) - SDF) / delta,
            (getSDF(dy + point) - SDF) / delta,
            (getSDF(dz + point) - SDF) / delta,
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