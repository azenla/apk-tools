package gay.pizza.pkg.apk.fs

import gay.pizza.pkg.apk.core.ApkInstalledDatabase
import gay.pizza.pkg.apk.index.ApkRawIndex
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.readLinesToList

class ApkFsInstalledDatabase(val path: FsPath) : ApkInstalledDatabase {
  override fun read(): ApkRawIndex {
    val lines = path.readLinesToList()
    return ApkRawIndex.parseIndexContent(lines.asSequence())
  }
}
