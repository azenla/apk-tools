package gay.pizza.pkg.apk.memory

import gay.pizza.pkg.apk.core.ApkWorld

class ApkMemoryWorld(var world: List<String> = emptyList()) : ApkWorld {
  override fun read(): List<String> = world

  override fun write(packages: List<String>) {
    world = packages
  }
}
