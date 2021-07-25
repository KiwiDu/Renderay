import kotlin.Result
import kotlin.math.abs

class TestResult(val obj: Obj?, val distance:Double) {
    fun hit():Boolean {
        return (abs(distance) < Epsilon) && obj!=null
    }
}

fun test(point:Vec3,scene: Scene,excludes:Obj?=null): TestResult {
    var closestObj: Obj? = null
    var minDistance: Double = Double.POSITIVE_INFINITY
    for (obj in scene.objects) {
        if (excludes == obj) continue
        val distance = obj.geo.getSDF(point)
        if (distance < minDistance) {
            closestObj = obj
            minDistance = distance
        }
    }
    return TestResult(closestObj, minDistance)
}