import data.Vec2
import data.Vec3
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.lang.Integer.min
import kotlin.system.*

fun main(args: Array<String>) = measureTimeMillis {
    render(720, 720,8)
}.run {
    val seconds = this % (60 * 1000)
    val minutes = this / (60 * 1000)
    println("Took ${minutes}min ${seconds.toDouble() / 1000}s.")
}

//@OptIn(ExperimentalCoroutinesApi::class)
fun render(W:Int, H:Int, S:Int)= runBlocking {
    val img = PPM(W, H)
    val scene = constructScene()
    val camera = getCamera(W,H)

    /*
    suspend fun superSamplingE(u: Double, v: Double)= channelFlow {
        val offsets = if (S > 0) (0..S).map { ran(it, S) } else listOf(data.Vec2(0.0, 0.0))
        for ((du,dv) in offsets) {
            launch {
                val ray = camera.emit(u + du, v + dv)
                ray.work(scene)
                send(ray.calc())
            }
        }
    }.flowOn(Dispatchers.Default)
    */
    suspend fun superSampling(u: Double, v: Double): Channel<Vec3> {
        val channel= Channel<Vec3>(S+1)
        val offsets = if (S > 0) (0..S).map { ran(it, S) } else listOf(Vec2(0.0, 0.0))
        for ((du,dv) in offsets) {
            launch(Dispatchers.Default) {
                val ray = camera.emit(u + du, v + dv)
                ray.work(scene)
                channel.send(ray.calc())
            }
        }
        return channel
    }

    data class Pixel(val i: Int, val j: Int, val color: Vec3)

    /*
    suspend fun doRenderF() = flow {
        for (i in 0 until H) {
            for (j in 0 until W) {
                val v = H.toDouble() / 2 - i - 0.5
                val u = j - W.toDouble() / 2 + 0.5
                emit(Pixel(i, j, superSampling(u, v)
                    .consumeAsFlow()
                    .take(S+1)
                    .reduce(data.Vec3::plus) / S.toDouble()))
            }
        }
    }
    */

    suspend fun doRenderC(): Channel<Pixel> {
        val channel = Channel<Pixel>(W * H)
        for (i in 0 until H) {
            for (j in 0 until W) {
                launch(Dispatchers.Default) {
                    val v = H.toDouble() / 2 - i - 0.5
                    val u = j - W.toDouble() / 2 + 0.5
                    channel.send(
                        Pixel(
                            i, j, superSampling(u, v)
                                .consumeAsFlow()
                                .take(S + 1)
                                .reduce(Vec3::plus) / (1+S).toDouble()
                        )
                    )
                }
            }
        }
        return channel
    }

    doRenderC().consumeAsFlow().take(W*H).collect { img.pixel(it.i, it.j, it.color)}

    img.writeTo("./IMG_OUT/img.ppm")
    "python ./IMG_OUT/main.py".exe()
}

fun getCamera(W:Int,H:Int) = Camera(
    //data.Vec3(0.0, 0.0, -20.0),
    //data.Vec3(0.0, 0.0, 1.0),
    Vec3(0.0, 15.0, -40.0),
    Vec3(0.0, -0.3, 1.0),
    1.0 / min(W, H),
    Camera.Mode.Perspective
)

fun constructScene(): Scene {
    val ball1 = Obj(
        Ball(Vec3(0.0, 0.0, -8.0), 3.5),
        Optic(
            Vec3(0.5, 0.5, 0.5),
            Vec3(0.0, 0.0, 0.0),
            Vec3(0.0, 0.0, 0.0),
            1.5, 2.0//, 0.1, 1.4,
        )
    )
    val ball2 = Obj(
        Ball(Vec3(-3.0, -3.0, -10.0), 1.0),
        Optic(Vec3(0.05, 0.05, 0.05), Vec3(1.022, 0.782, 0.344), Vec3(0.0, 0.0, 0.0), 4.0)
    )
    val ball3 = Obj(
        //Ball(data.Vec3(1.0, 2.0, -30.0), 2.0),
        OrthoBox(Vec3(1.0, 2.0, -8.0), Vec3(3.0, 2.0, 1.0)),
        Optic(
            Vec3(0.5, 0.5, 0.5),
            Vec3(0.0, 0.0, 0.0),
            Vec3(0.0, 0.0, 0.0),
            1.5,
        )
    )
    val cap1 = Obj(
        Capsule(Vec3(3.0, -2.0, 5.0), Vec3(-3.0, 2.0, 15.0), 1.0),
        Optic(
            Vec3(0.05, 0.05, 0.05),
            Vec3(0.955, 0.638, 0.538),
            Vec3(0.0, 0.0, 0.0),
            1.5, 0.0,
        )
    )
    val ground = Plane(Vec3(0.0, 1.0, 0.0), Vec3(0.0, -10.0, 0.0))
    val wall_bk = Plane(Vec3(0.0, 0.0, -1.0), Vec3(0.0, 0.0, 50.0))
    val wall_fr = Plane(Vec3(0.0, 0.0, 1.0), Vec3(0.0, 0.0, -50.0))
    val wall_l = Plane(Vec3(1.0, 0.0, 0.0), Vec3(-50.0, 0.0, 0.0))
    val wall_r = Plane(Vec3(-1.0, 0.0, 0.0), Vec3(50.0, 0.0, 0.0))
    val ceil = Plane(Vec3(0.0, -1.0, 0.0), Vec3(0.0, 50.0, 0.0))
    val bg = Obj(
        Shape.union(ground, wall_bk, wall_fr, wall_l, wall_r, ceil),
        Optic(Vec3(0.2, 0.2, 0.2), Vec3(0.8, 0.8, 0.8), Vec3(0.0, 0.0, 0.0), 15.0, 0.0)
    )
    return Scene(
        listOf(Light(Vec3(20.0, 10.0, -10.0), Vec3(1.0, 1.0, 1.0))),
        listOf(bg, ball2, ball3)
        //listOf(cap1)
    )
}