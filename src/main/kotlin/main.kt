import kotlinx.coroutines.*
import java.lang.Integer.min
import kotlin.system.*

fun main(args: Array<String>) = measureTimeMillis {
    render(1024, 1024,10)
}.run {
    val seconds = this % (60 * 1000)
    val minutes = this / (60 * 1000)
    println("Took ${minutes}min ${seconds.toDouble() / 1000}s.")
}

fun render(W:Int, H:Int, S:Int) {
    val img = PPM(W, H)
    val scene = constructScene()
    val camera = Camera(
        Vec3(0.0, 0.0, -25.0),
        Vec3(0.0, 0.0, 1.0),
        1.0 / min(W, H),
        Camera.Mode.Perspective
    )

    suspend fun doRender() = coroutineScope {
        for (i in 0 until H) {
            for (j in 0 until W) {
                launch {
                    val v = H.toDouble() / 2 - i - 0.5
                    val u = j - W.toDouble() / 2 + 0.5
                    val offsets = if (S > 0) (0..S).map { ran(it, S) } else listOf(Vec2(0.0, 0.0))
                    val samples = offsets.map {
                        val ray = camera.emit(u + it.x, v + it.y)
                        ray.work(scene)
                        ray.calc()
                    }
                    val color = samples.reduce(Vec3::plus) / (samples.size).toDouble()

                    img.pixel(i, j, color)
                }
            }
        }
    }

    runBlocking {
        doRender()
    }
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