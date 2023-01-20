package gay.pizza.pkg.apk

interface ApkRepositoryList {
  val repositories: List<String>

  val indexDownloadUrls: List<String>
    get() = repositories.map { "${it}/APKINDEX.tar.gz" }
}
