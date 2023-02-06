package gay.pizza.pkg.apk.fs

import gay.pizza.pkg.apk.index.ApkIndex
import gay.pizza.pkg.apk.index.ApkIndexCollection
import gay.pizza.pkg.apk.index.ApkIndexFile
import gay.pizza.pkg.fetch.ContentFetcher
import gay.pizza.pkg.fetch.FetchRequest
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.delete
import gay.pizza.pkg.io.isDirectory
import gay.pizza.pkg.io.list

class ApkFsIndexCollection(val path: FsPath, val fetcher: ContentFetcher) : ApkIndexCollection {
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
    val hashCode = url.hashCode()
    val hex = hashCode.toString(16).replace("-", "n")
    val indexDownloadPath = path.resolve("APKINDEX.${hex}.tar.gz")
    val request = FetchRequest(url, userAgent = "apk-tools-kotlin/1.0")
    fetcher.download(request, indexDownloadPath)
    _indexes.add(ApkIndexFile(indexDownloadPath))
  }

  override fun clean() {
    _indexes.map { it.path }.forEach { it.delete() }
    _indexes.clear()
  }
}
