package gay.pizza.pkg.apk

interface ApkPackageCache {
  fun acquire(pkg: ApkIndexPackage, repositoryList: ApkRepositoryList): ApkPackageFile
}
