package gay.pizza.pkg.apk.core

interface ApkRepositoryList {
  val repositories: List<String>

  val indexDownloadUrls: List<String>
    get() = repositories.map { "${it}/APKINDEX.tar.gz" }
}
