package data

class Mat3(val a11:Double,val a12:Double,val a13:Double,
           val a21:Double,val a22:Double,val a23:Double,
           val a31:Double,val a32:Double,val a33:Double,) {
    companion object {
        fun Row(row1: Vec3, row2: Vec3, row3: Vec3): Mat3 {
            return Mat3(
                row1.x, row1.y, row1.z,
                row2.x, row2.y, row2.z,
                row3.x, row3.y, row3.z,
            )
        }

        fun Col(col1: Vec3, col2: Vec3, col3: Vec3): Mat3 {
            return Mat3(
                col1.x, col2.x, col3.x,
                col1.y, col2.y, col3.y,
                col1.z, col2.z, col3.z,
            )
        }

        fun Zeros() = Mat3(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,)
        fun Ones() = Mat3(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,)
        fun Unit() = Mat3(
            1.0, 0.0, 0.0,
            0.0, 1.0, 0.0,
            0.0, 0.0, 1.0,
        )
    }
}