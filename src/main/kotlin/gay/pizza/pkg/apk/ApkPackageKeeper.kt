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

  fun download(packages: Iterable<ApkIndexPackage>) {
    for (pkg in packages) {
      provider.packageCache.acquire(pkg, provider.repositoryList)
    }
  }
}
