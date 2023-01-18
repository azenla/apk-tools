package gay.pizza.pkg.apk

import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.createDirectories

class ApkPackageKeeper(val root: FsPath) {
  val cache = ApkPackageCache(root.resolve("var/cache/apk").apply { createDirectories() })

  private val _index: ApkIndex? = null
  private val _graph: ApkIndexGraph? = null

  private fun loadApkIndex() {
    
  }

  fun download(packages: Iterable<ApkIndexPackage>) {
    for (pkg in packages) {
      cache.acquire(pkg)
    }
  }

  fun install(packages: Iterable<ApkIndexPackage>) {
    packages.map {  }
  }
}