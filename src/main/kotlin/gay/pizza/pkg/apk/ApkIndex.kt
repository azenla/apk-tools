package gay.pizza.pkg.apk

import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.java.toJavaPath
import java.net.URL

class ApkIndex(
  val packages: List<ApkIndexPackage>
) {
  fun packageById(id: String): ApkIndexPackage = packages.first { it.id == id }
  fun findById(id: String): ApkIndexPackage? = packages.firstOrNull { it.id == id }

  companion object {
    fun extract(repo: String, index: ApkRawIndex): ApkIndex =
      ApkIndex(index.packages.map { pkg -> ApkIndexPackage.extract(repo, pkg) })

    fun merge(indexes: List<ApkIndex>): ApkIndex = ApkIndex(indexes.map { it.packages }.flatten())
    fun merge(vararg indexes: ApkIndex): ApkIndex = ApkIndex(indexes.map { it.packages }.flatten())

    fun loadByUrl(url: String): ApkIndex {
      val javaUrl = URL("${url}/APKINDEX.tar.gz")
      val urlStream = javaUrl.openStream().buffered()
      val raw = ApkRawIndex.parseGzipTarIndex(urlStream)
      return extract(url, raw)
    }

    fun loadByPath(url: String, path: FsPath): ApkIndex {
      val stream = path.toJavaPath().toFile().inputStream().buffered()
      val raw = ApkRawIndex.parseGzipTarIndex(stream)
      return extract(url, raw)
    }
  }
}
