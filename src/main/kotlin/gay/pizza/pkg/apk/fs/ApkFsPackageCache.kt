package gay.pizza.pkg.apk.fs

import gay.pizza.pkg.apk.core.ApkPackageCache
import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.apk.index.ApkPackageNotFoundException
import gay.pizza.pkg.apk.core.ApkRepositoryList
import gay.pizza.pkg.apk.index.ApkIndexPackage
import gay.pizza.pkg.io.FsPath

class ApkFsPackageCache(val path: FsPath) : ApkPackageCache {
  override fun acquire(pkg: ApkIndexPackage, repositoryList: ApkRepositoryList): ApkPackageFile {
    val packageFilePath = path.resolve(pkg.downloadFileName)
    val potentialUrls = repositoryList.repositories.map { "${it}/${pkg.downloadFileName}" }
    for (url in potentialUrls) {
      try {
        return ApkPackageFile.download(url, packageFilePath)
      } catch (_: ApkPackageNotFoundException) {
        continue
      }
    }
    throw RuntimeException("Failed to acquire package file ${pkg.downloadFileName}")
  }
}
