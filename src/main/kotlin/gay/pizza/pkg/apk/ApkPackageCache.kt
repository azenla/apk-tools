package gay.pizza.pkg.apk

import gay.pizza.pkg.io.FsPath

class ApkPackageCache(val path: FsPath) {
  fun acquire(pkg: ApkIndexPackage): ApkPackageFile {
    val packageFilePath = path.resolve("${pkg.id}-${pkg.version}.apk")
    return ApkPackageFile.download(url = pkg.downloadUrl, packageFilePath)
  }
}
