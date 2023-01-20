package gay.pizza.pkg.apk

import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.delete
import gay.pizza.pkg.io.isDirectory
import gay.pizza.pkg.io.java.toJavaPath
import gay.pizza.pkg.io.list
import java.net.URL
import kotlin.io.path.outputStream

class ApkFsIndexCollection(val path: FsPath) : ApkIndexCollection {
  private var _indexes = mutableListOf<ApkIndexFile>()

  val indexFiles: List<ApkIndexFile>
    get() = _indexes

  override val indexes: List<ApkIndex>
    get() = _indexes.map { it.read() }

  override val index: ApkIndex
    get() = ApkIndex.merge(indexes)

  init {
    loadAll()
  }

  private fun loadAll() {
    _indexes = path.list().filter { file ->
      !file.isDirectory() &&
        file.entityNameString.contains("APKINDEX.") &&
        file.entityNameString.endsWith(".tar.gz")
    }.map { ApkIndexFile(it) }.toMutableList()
  }

  override fun download(url: String) {
    val javaUrl = URL(url)
    val stream = javaUrl.openStream()
    val indexDownloadPath = path.resolve("APKINDEX.${javaUrl.hashCode()}.tar.gz")
    val out = indexDownloadPath.toJavaPath().outputStream()
    stream.copyTo(out)
    stream.close()
    out.close()
    _indexes.add(ApkIndexFile(indexDownloadPath))
  }

  override fun clean() {
    _indexes.map { it.path }.forEach { it.delete() }
    _indexes.clear()
  }
}
