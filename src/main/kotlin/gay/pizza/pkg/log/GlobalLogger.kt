package gay.pizza.pkg.log

import kotlin.system.measureTimeMillis

object GlobalLogger {
  fun warn(message: String) {
    System.err.println("[WARNING] $message")
  }

  fun debug(message: String) {
    if (System.getenv("APK_TOOLS_LOG_VERBOSE") != "1") {
      return
    }
    System.err.println("[DEBUG] $message")
  }

  fun <T> timed(name: String, block: () -> T): T {
    debug("Started $name")
    var result: T
    val time = measureTimeMillis {
      result = block()
    }
    debug("Finished $name in ${time}ms")
    return result
  }
}
