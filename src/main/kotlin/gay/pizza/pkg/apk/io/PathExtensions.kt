package gay.pizza.pkg.apk.io

import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.java.toJavaPath
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

fun FsPath.outputStream(
  vararg options: OpenOption = arrayOf(StandardOpenOption.TRUNCATE_EXISTING)
): OutputStream =
  toJavaPath().outputStream(*options)

fun FsPath.inputStream(): InputStream =
  toJavaPath().inputStream()
