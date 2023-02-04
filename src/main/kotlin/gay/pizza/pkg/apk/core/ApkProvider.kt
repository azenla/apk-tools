package gay.pizza.pkg.apk.core

import gay.pizza.pkg.apk.frontend.ApkKeeper
import gay.pizza.pkg.apk.index.ApkIndex
import gay.pizza.pkg.apk.index.ApkIndexCollection
import gay.pizza.pkg.io.FsPath

class ApkProvider {
  lateinit var packageCache: ApkPackageCache
  lateinit var indexCollection: ApkIndexCollection
  lateinit var repositoryList: ApkRepositoryList
  lateinit var systemRootPath: FsPath
  lateinit var installedDatabase: ApkInstalledDatabase
  lateinit var world: ApkWorld
  lateinit var arches: ApkSupportedArches

  val index: ApkIndex
    get() = indexCollection.index

  val keeper by lazy { ApkKeeper(this) }
}
