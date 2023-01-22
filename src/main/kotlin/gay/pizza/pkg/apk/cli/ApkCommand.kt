package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import gay.pizza.pkg.PlatformPath
import gay.pizza.pkg.apk.core.ApkProvider
import gay.pizza.pkg.apk.frontend.ApkPackageKeeper
import gay.pizza.pkg.apk.fs.*
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.fsPath

class ApkCommand : CliktCommand(help = "Alpine Package Keeper", name = "apk", invokeWithoutSubcommand = true) {
  val repositoryFilePath by option("--repository-file-path", "-R", help = "Repository File Path").fsPath()
  val packageCachePath by option("--package-cache-path", "-C", help = "Package Cache Path").fsPath()
  val installedDatabasePath by option("--installed-database-path", "-D", help = "Installed Database Path").fsPath()
  val worldFilePath by option("--world-file-path", "-W", help = "World File Path").fsPath()
  val archesFilePath by option("--arch-file-path", "-A", help = "Supported Architectures File Path").fsPath()
  val systemRootPath by option("--root", "-r", help = "System Root Path").fsPath().default(PlatformPath("sysroot"))

  override fun run() {
    fun FsPath?.defaultSysrootRelative(path: String): FsPath {
      if (this != null) return this
      return systemRootPath.resolve(path)
    }

    val provider = ApkProvider()
    provider.repositoryList = ApkFsRepositoryList(repositoryFilePath.defaultSysrootRelative("etc/apk/repositories"))
    provider.packageCache = ApkFsPackageCache(packageCachePath.defaultSysrootRelative("var/cache/apk"))
    provider.indexCollection = ApkFsIndexCollection(packageCachePath.defaultSysrootRelative("var/cache/apk"))
    provider.installedDatabase = ApkFsInstalledDatabase(installedDatabasePath.defaultSysrootRelative("lib/apk/db/installed"))
    provider.world = ApkFsWorld(worldFilePath.defaultSysrootRelative("etc/apk/world"))
    provider.arches = ApkFsSupportedArches(archesFilePath.defaultSysrootRelative("etc/apk/arch"))
    provider.systemRootPath = systemRootPath
    currentContext.findOrSetObject { ApkPackageKeeper(provider) }
  }
}
