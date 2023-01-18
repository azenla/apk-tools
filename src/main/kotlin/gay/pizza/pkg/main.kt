package gay.pizza.pkg

import gay.pizza.pkg.apk.ApkIndex
import gay.pizza.pkg.apk.ApkIndexGraph
import gay.pizza.pkg.apk.ApkPackageCache
import gay.pizza.pkg.apk.ApkPackageInstaller
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.createDirectories
import gay.pizza.pkg.io.deleteRecursively
import gay.pizza.pkg.io.exists

fun FsPath.deleteAndRecreate(): FsPath {
  if (exists()) deleteRecursively()
  createDirectories()
  return this
}

fun alpineReleaseIndexes(version: String, arch: String): ApkIndex {
  val main = ApkIndex.loadByUrl("https://dl-cdn.alpinelinux.org/alpine/${version}/main/${arch}")
  val community = ApkIndex.loadByUrl("https://dl-cdn.alpinelinux.org/alpine/${version}/community/${arch}")
  return ApkIndex.merge(main, community)
}

fun adelieReleaseIndexes(version: String, arch: String): ApkIndex {
  val system = ApkIndex.loadByUrl("https://distfiles.adelielinux.org/adelie/${version}/system/${arch}")
  val user = ApkIndex.loadByUrl("https://distfiles.adelielinux.org/adelie/${version}/user/${arch}")
  return ApkIndex.merge(system, user)
}

fun main() {
  val packageCacheDirectory = PlatformPath("pkgs").deleteAndRecreate()
  val sysrootDirectory = PlatformPath("sysroot").deleteAndRecreate()
  val index = alpineReleaseIndexes("v3.17", "aarch64")
  val graph = ApkIndexGraph(index)
  val cache = ApkPackageCache(packageCacheDirectory)
  val installPackageTree = graph.tree(index.packageById("git"))
  val installer = ApkPackageInstaller(cache, sysrootDirectory)
  installer.download(installPackageTree.packages)
  installer.extract()
}
