package gay.pizza.pkg.io.java

import gay.pizza.pkg.io.FsPath
import java.nio.file.Path

fun Path.toFsPath(): FsPath = JavaPath(this)
fun FsPath.toJavaPath(): Path {
  return if (this is JavaPath) {
    javaPath
  } else {
    Path.of(fullPathString)
  }
}
