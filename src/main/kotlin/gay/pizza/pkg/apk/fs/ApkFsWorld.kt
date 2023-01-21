package gay.pizza.pkg.apk.fs

import gay.pizza.pkg.apk.core.ApkWorld
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.readLinesToList
import gay.pizza.pkg.io.writeString

class ApkFsWorld(val path: FsPath) : ApkWorld {
  override fun read(): List<String> = path.readLinesToList()
    .map { it.trim() }
    .filter { it.isNotBlank() }

  override fun write(packages: List<String>) {
    path.writeString(packages.joinToString("\n") + "\n")
  }
}
