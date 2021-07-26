import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.internal.ChannelFlow
import java.lang.Integer.min
import kotlin.system.*

fun main(args: Array<String>) = measureTimeMillis {
    /*
    suspend fun f()= channelFlow {
            for (i in 0..9) {
                launch {
                    delay((10 - i) * 100L)
                    print(i)
                    print(" ")
                    send(i)
                }
            }
    }
    */
    /*
    runBlocking {
        val f = Channel<Int>()
        for (i in 0..9) {
            launch {
                delay((10 - i) * 100L)
                print("$i ")
                f.send(i)
            }
        }

        launch { println(f.consumeAsFlow().take(10).reduce { a, b -> a + b }) }
    }
    */
    render(1920/2, 1080/2,8)
}.run {
    val seconds = this % (60 * 1000)
    val minutes = this / (60 * 1000)
    println("Took ${minutes}min ${seconds.toDouble() / 1000}s.")
}

//@OptIn(ExperimentalCoroutinesApi::class)
fun render(W:Int, H:Int, S:Int)= runBlocking {
    val img = PPM(W, H)
    val scene = constructScene()
    val camera = Camera(
        Vec3(0.0, 0.0, -25.0),
        Vec3(0.0, 0.0, 1.0),
        1.0 / min(W, H),
        Camera.Mode.Perspective
    )

    /*
    suspend fun superSamplingE(u: Double, v: Double)= channelFlow {
        val offsets = if (S > 0) (0..S).map { ran(it, S) } else listOf(Vec2(0.0, 0.0))
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
                    .reduce(Vec3::plus) / S.toDouble()))
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
                                .reduce(Vec3::plus) / S.toDouble()
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

fun constructScene(): Scene {
    val ball1 = Obj(
        Ball(Vec3(1.0, 2.0, -8.0), 3.5),
        //Texture(Vec3(1.0, 0.0, 0.0), Vec3(0.2, 0.2, 0.2), 10.0, 2.0)
        Texture(
            Vec3(0.5, 0.5, 0.5),
            Vec3(0.0, 0.0, 0.0),
            1.5, 0.0, 0.1, 1.5,
        )
    )
    val ball2 = Obj(
        Ball(Vec3(3.0, 2.0, 0.0), 3.0),
        Texture(Vec3(0.05, 0.05, 0.05), Vec3(1.022, 0.782, 0.344), 4.0)
    )
    val cap1 = Obj(
        Capsule(Vec3(3.0, -2.0, 5.0), Vec3(-3.0, 2.0, 15.0), 1.0),
        Texture(
            Vec3(0.05, 0.05, 0.05),
            Vec3(0.955, 0.638, 0.538),
            1.5, 0.0,
        )
    )
    val ground = Obj(
        Plane(Vec3(0.0, 1.0, 0.0), Vec3(0.0, -3.0, 0.0)),
        Texture(Vec3(0.2, 0.2, 0.2), Vec3(0.8, 0.8, 0.8), 15.0, 2.0)
    )
    val wall = Obj(
        Plane(Vec3(0.0, 0.0, -1.0), Vec3(0.0, 0.0, 25.0)),
        Texture(Vec3(0.2, 0.2, 0.2), Vec3(0.8, 0.8, 0.8), 15.0, 0.0)
    )
    val bg = Obj(
        Shape.union(ground.geo,wall.geo),
        Texture(Vec3(0.2, 0.2, 0.2), Vec3(0.8, 0.8, 0.8), 15.0, 0.0)
    )
    return Scene(
        listOf(Light(Vec3(0.0, 10.0, -10.0), Vec3(1.0, 1.0, 1.0))),
        listOf(bg,cap1)
        //listOf(cap1)
    )
}