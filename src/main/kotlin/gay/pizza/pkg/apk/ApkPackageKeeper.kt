package gay.pizza.pkg.apk

class ApkPackageKeeper(val provider: ApkProvider) {
  val index: ApkIndex
    get() = provider.indexCollection.index

  fun update() {
    provider.indexCollection.clean()
    provider.repositoryList.indexDownloadUrls.forEach { url ->
      provider.indexCollection.download(url)
    }
  }

  fun download(packages: Iterable<ApkIndexPackage>): List<ApkPackageFile> {
    val files = mutableListOf<ApkPackageFile>()
    for (pkg in packages) {
      val file = provider.packageCache.acquire(pkg, provider.repositoryList)
      files.add(file)
    }
    return files
  }

  fun install(files: Iterable<ApkPackageFile>) {
    for (file in files) {
      file.extract(provider.systemRootPath)
    }
  }
}
