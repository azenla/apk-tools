package gay.pizza.pkg.apk.fs

import gay.pizza.pkg.apk.index.ApkIndex
import gay.pizza.pkg.apk.index.ApkIndexCollection
import gay.pizza.pkg.apk.index.ApkIndexFile
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.delete
import gay.pizza.pkg.io.isDirectory
import gay.pizza.pkg.io.java.toJavaPath
import gay.pizza.pkg.io.list
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import kotlin.io.path.outputStream

class ApkFsIndexCollection(val path: FsPath, val httpClient: HttpClient) : ApkIndexCollection {
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
    val request = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create(url))
      .header("User-Agent", "apk-tools-kotlin/1.0")
      .build()
    val response = httpClient.send(request, BodyHandlers.ofFile(indexDownloadPath.toJavaPath()))
    if (response.statusCode() != 200) {
      indexDownloadPath.delete()
      throw RuntimeException("Index download of $url failed (Status Code ${response.statusCode()})")
    }
    _indexes.add(ApkIndexFile(indexDownloadPath))
  }

  override fun clean() {
    _indexes.map { it.path }.forEach { it.delete() }
    _indexes.clear()
  }
}
