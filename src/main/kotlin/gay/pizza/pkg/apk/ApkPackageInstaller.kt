package gay.pizza.pkg.apk

import gay.pizza.pkg.io.FsPath

class ApkPackageInstaller(val cache: ApkPackageCache, val root: FsPath) {
  internal val files: MutableList<ApkPackageFile> = mutableListOf()

  fun download(packages: Set<ApkIndexPackage>) {
    for (pkg in packages) {
      val file = cache.acquire(pkg)
      synchronized(files) {
        files.add(file)
      }
    }
  }

  fun extract() {
    for (file in files) {
      file.extract(root)
    }
  }

  fun install(packages: Set<ApkIndexPackage>) {
    download(packages)
    extract()
  }
}