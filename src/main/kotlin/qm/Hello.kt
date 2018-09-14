package qm

import io.javalin.Javalin
import net.openhft.chronicle.map.ChronicleMap
import org.slf4j.impl.SimpleLogger
import java.io.File
import java.io.Serializable
import java.util.*

class MyIntWrapper(val value:Int):Serializable

fun main(args: Array<String>) {
    configureLogging()

    val dataFileName = getDataFileName()

    println(dataFileName)

    val uuid = UUID.randomUUID()

    val map  = createChronicleMap()

    val app = Javalin.create().start(7000)

    app.get("/") {
        ctx ->
        val count = map[uuid] ?: MyIntWrapper(0)
        map[uuid] = MyIntWrapper(count.value + 1)
        val result = """
            Hello World: ${count.value}
            Data file name: ${dataFileName}
            Keys: ${map.keys}
        """.trimIndent()
        ctx.result(result)
    }
}

fun configureLogging() {
    System.setProperty(SimpleLogger.LOG_FILE_KEY, "System.err")
}

fun createChronicleMap(): ChronicleMap<UUID, MyIntWrapper> {
    return ChronicleMap
            .of(UUID::class.java, MyIntWrapper::class.java)
            .name("access-count-map")
            .averageKey(UUID(10, 10))
            .averageValueSize(10.0)
            .entries(10000)
            .createOrRecoverPersistedTo(getDataFileName())
}

fun getDataFileName() = File("${System.getProperty("java.io.tmpdir")}${System.getProperty("file.separator")}access-count-map.dat")

