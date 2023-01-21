package gay.pizza.pkg.apk.core

import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.apk.index.ApkIndexPackage

interface ApkPackageCache {
  fun acquire(pkg: ApkIndexPackage, repositoryList: ApkRepositoryList): ApkPackageFile
}
