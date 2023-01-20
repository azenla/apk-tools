package gay.pizza.pkg.apk

import gay.pizza.pkg.io.FsPath

class ApkProvider {
  lateinit var packageCache: ApkPackageCache
  lateinit var indexCollection: ApkIndexCollection
  lateinit var repositoryList: ApkRepositoryList
  lateinit var systemRootPath: FsPath
}
