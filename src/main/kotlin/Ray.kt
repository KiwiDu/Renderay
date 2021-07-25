import java.lang.Double.max
import kotlin.math.pow

fun screen(a: Vec3, b: Vec3): Vec3 {
    return 1.0 - (1.0 - a) * (1.0 - b)
}

class Ray(var position: Vec3, var direction: Vec3, private var inside:Obj?=null) {
    private val MAX_TIMES: Int = 1024
    private val MAX_DISTANCE: Double = 256.0

    private val colors = MutableList(1) { Vec3(0.0, 0.0, 0.0) }

    private var _times: Int = 0
    private var _distance: Double = 0.0

    private fun march(distance: Double, count: Boolean = true) {
        position += direction.unit() * distance
        if (count) _times++
        _distance += distance
    }

    private fun test(scene: Scene, excludes: Obj? = null): TestResult {
        return test(position, scene, excludes)
    }

    private fun derive(redirect: Vec3): Ray {
        return Ray(position, redirect).also {
            it._distance = _distance
            it._times = _times
            it.march(2 * Epsilon)
        }
    }

    fun work(scene: Scene) {

        wk@ while (_times < MAX_TIMES && _distance < MAX_DISTANCE) {
            val inOrOut = if (test(position, scene).distance < 0.0) -1.0 else 1.0
            val result = test(scene)
            if (result.hit()) {
                val objHit: Obj = result.obj as Obj
                val fullReflect:Boolean
                if (objHit.tex.opacity < 1.0) {

                    val newDirection = objHit.calcRefraction(direction, position, objHit.tex.eta, inOrOut)
                    if (newDirection != null) {
                        fullReflect=false
                        val newRay = derive(newDirection)
                        while (objHit.geo.getSDF(newRay.position) < 0.0) {
                            val res = newRay.test(scene, excludes = objHit)
                            newRay.march(res.distance)
                        }
                        newRay.march(-objHit.geo.getSDF(newRay.position))
                        newRay.march(-Epsilon * 5)
                        newRay.work(scene)
                        colors.add(newRay.calc() * (1.0 - objHit.tex.opacity))
                    }else {
                        fullReflect = true
                    }
                }else {
                    fullReflect = false
                }
                if (objHit.tex.reflectivity > 1.0 || fullReflect) {//Full reflection
                    colors.addAll(lighting(scene, objHit, Comp.DIFF, Comp.EM))
                    direction = (inOrOut*objHit.geo.getNormal(position)).reflect(direction)
                    march(2 * Epsilon, false)
                    continue
                }
                colors.addAll(lighting(scene, objHit).map { objHit.tex.opacity * it })
                if (objHit.tex.reflectivity > 0.0) {//Partial reflection
                    val newRay = derive(
                        (inOrOut*objHit.geo.getNormal(position)).reflect(direction)
                    )
                    newRay.work(scene)
                    val newLight = Light(newRay.position, newRay.calc() * objHit.tex.reflectivity)
                    colors.add(lighting(newLight, result.obj))
                }
                break
            } else {
                march(result.distance * inOrOut)
            }
        }
    }

    private enum class Comp {
        EM, AMB, DIFF, SPEC
    }

    private fun lighting(scene: Scene, obj: Obj, vararg ex: Comp): List<Vec3> {
        return scene.lights.map { lighting(it, obj, *ex) }
    }
    
    private fun lighting(light: Light, obj: Obj, vararg ex: Comp): Vec3 {
        val L = (light.position - position).unit()
        val V = -direction.unit()
        val N = obj.geo.getNormal(position)

        val H = (L + V).unit()

        fun Comp.h(calc: () -> Vec3): Vec3 {
            return if (this in ex)
                Vec3(0.0, 0.0, 0.0)
            else
                calc()
        }

        val emissive = Vec3(0.0, 0.0, 0.0)
        val ambient = Comp.AMB.h { Vec3(0.1, 0.1, 0.1) }
        val diffuse = Comp.DIFF.h { obj.tex.diff * max(L dot N, 0.0) }
        val specular = Comp.SPEC.h { obj.tex.spec * max(N dot H, 0.0).pow(obj.tex.a) }

        return light.color * (emissive + ambient + specular + diffuse)
    }

    fun calc(): Vec3 {
        return colors.reduce(::screen)
    }
}