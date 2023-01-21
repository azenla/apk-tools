package gay.pizza.pkg.apk.index

import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.java.toJavaPath
import java.io.InputStream
import kotlin.io.path.outputStream

class ApkIndexFile(val path: FsPath) {
  var index: ApkIndex? = null

  fun read(): ApkIndex = index ?: load()

  fun load(): ApkIndex {
    val loaded = ApkIndex.loadByPath(path)
    index = loaded
    return loaded
  }

  fun write(data: InputStream): ApkIndex {
    path.toJavaPath().outputStream().use { data.copyTo(it) }
    return load()
  }
}
