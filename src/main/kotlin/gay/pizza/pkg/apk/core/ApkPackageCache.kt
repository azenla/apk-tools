package gay.pizza.pkg.apk.core

import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.apk.index.ApkIndexPackage

interface ApkPackageCache {
  fun read(pkg: ApkIndexPackage): ApkPackageFile?
  fun acquire(pkg: ApkIndexPackage, repositoryList: ApkRepositoryList): ApkPackageFile
}
