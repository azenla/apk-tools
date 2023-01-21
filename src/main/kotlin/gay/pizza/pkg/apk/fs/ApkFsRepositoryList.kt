package gay.pizza.pkg.apk.fs

import gay.pizza.pkg.apk.core.ApkRepositoryList
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.readLinesToList

class ApkFsRepositoryList(val path: FsPath) : ApkRepositoryList {
  private var repositoryLines: List<String> = mutableListOf()

  override val repositories: List<String>
    get() = repositoryLines

  init {
    reload()
  }

  fun reload() {
    repositoryLines = path.readLinesToList().filter { it.trim().isNotBlank() }
  }
}
