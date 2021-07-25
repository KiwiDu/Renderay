data class Texture(val diff:Vec3,
                   val spec:Vec3,
                   val a:Double,
                   val reflectivity:Double=0.0,
                   val opacity:Double=1.0,
                   val eta:Double=1.0)