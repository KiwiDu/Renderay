import java.io.InputStreamReader
import java.io.LineNumberReader

fun String.exe(){
    try {
        val process = Runtime.getRuntime().exec(this)
        val ir = InputStreamReader(process.inputStream)
        val er = InputStreamReader(process.errorStream)
        val input = LineNumberReader(ir)
        val error = LineNumberReader(er)
        input.forEachLine(::println)
        println("---------------------")
        error.forEachLine(::println)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
