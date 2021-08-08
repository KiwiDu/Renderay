import data.Vec3

data class Optic(
    val diff: Vec3,
    val spec: Vec3,
    val em: Vec3,
    val a:Double,
    val reflectivity:Double=0.0,
    val opacity:Double=1.0,
    val eta:Double=1.0)

/*
interface Optic{
    fun shade(L: data.Vec3,V:data.Vec3,N:data.Vec3):data.Vec3
}
data class BasicOptics(val color:data.Vec3): Optic{
    override fun shade(L: data.Vec3, V: data.Vec3, N: data.Vec3) = color
}
data class BasicOptics(val color:data.Vec3): Optic{
    override fun shade(L: data.Vec3, V: data.Vec3, N: data.Vec3) = color
}
 */