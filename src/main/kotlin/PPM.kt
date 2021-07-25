import java.io.File
import java.nio.charset.Charset

class PPM(private val width:Int, private val height:Int) {
    private val data :ByteArray= ByteArray(3*width*height)
    fun writeTo(src:String) {
        val file= File(src).run{
            if(exists()){
                delete()
            }
            createNewFile()
            writeText("P6 $width $height 255 ", Charset.forName("ASCII"))
            appendBytes(data)
        }
    }
    fun pixel(i:Int,j:Int,color:Vec3){
        val pos=i*width+j
        val (R,G,B) = (color*255.0).forEachG { it.toInt().truncate(0,255).toByte() }

        data[3*pos]=R
        data[3*pos+1]=G
        data[3*pos+2]=B
    }
}