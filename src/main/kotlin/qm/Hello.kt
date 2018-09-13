package qm

import io.javalin.Javalin
import net.openhft.chronicle.map.ChronicleMap
import org.slf4j.impl.SimpleLogger
import java.io.File
import java.io.Serializable
import java.util.*

class MyIntWrapper(val value:Int):Serializable

fun main(args: Array<String>) {
    System.setProperty(SimpleLogger.LOG_FILE_KEY, "System.err")

    val uuid = UUID.randomUUID()
    val map  = ChronicleMap
            .of(UUID::class.java, MyIntWrapper::class.java)
            .name("access-count-map")
            .averageKey(UUID(10, 10))
            .averageValueSize(10.0)
            .entries(10000)
            .createOrRecoverPersistedTo(File(System.getProperty("java.io.tmpdir") + "access-count-map.dat"))

    val app = Javalin.create().start(7000)

    val tempDir = System.getProperty("java.io.tmpdir")

    app.get("/") {
        ctx ->
        val count = map[uuid] ?: MyIntWrapper(0)
        map[uuid] = MyIntWrapper(count.value + 1)
        val result = """
            |Hello World: ${count.value}
            |java.io.tmpdir: ${tempDir}
            |Keys: ${map.keys}
        """.trimIndent()
        ctx.result(result)
    }
}

