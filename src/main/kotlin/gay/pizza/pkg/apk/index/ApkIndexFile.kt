package gay.pizza.pkg.apk.index

import gay.pizza.pkg.apk.io.outputStream
import gay.pizza.pkg.io.FsPath
import java.io.InputStream

class ApkIndexFile(val path: FsPath) {
  var index: ApkIndex? = null

  fun read(): ApkIndex = index ?: load()

  fun load(): ApkIndex {
    val loaded = ApkIndex.loadByPath(path)
    index = loaded
    return loaded
  }

  fun write(data: InputStream): ApkIndex {
    path.outputStream().use { data.copyTo(it) }
    return load()
  }
}
