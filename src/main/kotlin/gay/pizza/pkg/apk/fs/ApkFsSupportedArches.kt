package gay.pizza.pkg.apk.fs

import gay.pizza.pkg.apk.core.ApkArchitecture
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.readLinesToList
import gay.pizza.pkg.io.writeString

class ApkFsSupportedArches(val path: FsPath) : ApkArchitecture {
  override fun read(): List<String> {
    return path.readLinesToList().map { it.split(" ") }.flatten()
  }

  override fun write(arches: List<String>) {
    path.writeString(arches.joinToString("\n") + "\n")
  }
}
