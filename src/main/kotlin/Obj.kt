import kotlin.math.sqrt

class Obj(val geo:Shape, val tex:Texture) {
    fun calcReflection(iDirection: Vec3, point: Vec3): Vec3 {
        return geo.getNormal(point).reflect(iDirection)
    }

    fun calcRefraction(iDirection: Vec3, point: Vec3, eta_ratio: Double, inOrOut: Double): Vec3? {
        val e = if (inOrOut > 0) eta_ratio else 1.0 / eta_ratio
        val I = iDirection.unit()
        val N = (geo.getNormal(point) * inOrOut).unit()
        val IdotN = I dot N
        val k = 1.0 - e * e * (1.0 - IdotN * IdotN)
        if (k < 0.0) {
            return null
        }
        val a = e * IdotN + sqrt(k)
        return e * I - a * N
    }
}