package gay.pizza.pkg.apk.fs

import gay.pizza.pkg.apk.core.ApkPackageCache
import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.apk.index.ApkPackageNotFoundException
import gay.pizza.pkg.apk.core.ApkRepositoryList
import gay.pizza.pkg.apk.index.ApkIndexPackage
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.exists
import java.net.http.HttpClient

class ApkFsPackageCache(val path: FsPath, val httpClient: HttpClient, val forceDownload: Boolean = false) : ApkPackageCache {
  override fun read(pkg: ApkIndexPackage): ApkPackageFile? {
    val packageFilePath = path.resolve(pkg.downloadFileName)
    return if (packageFilePath.exists()) {
      ApkPackageFile(packageFilePath)
    } else {
      null
    }
  }

  override fun acquire(pkg: ApkIndexPackage, repositoryList: ApkRepositoryList): ApkPackageFile {
    val packageFilePath = path.resolve(pkg.downloadFileName)
    if (packageFilePath.exists() && !forceDownload) {
      return ApkPackageFile(packageFilePath)
    }
    val potentialUrls = repositoryList.repositories.map { "${it}/${pkg.downloadFileName}" }
    for (url in potentialUrls) {
      try {
        return ApkPackageFile.download(url, packageFilePath, client = httpClient)
      } catch (_: ApkPackageNotFoundException) {
        continue
      }
    }
    throw RuntimeException("Failed to acquire package file ${pkg.downloadFileName}")
  }
}
