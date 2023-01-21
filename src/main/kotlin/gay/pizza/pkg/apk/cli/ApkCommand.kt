package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import gay.pizza.pkg.apk.core.ApkProvider
import gay.pizza.pkg.apk.frontend.ApkPackageKeeper
import gay.pizza.pkg.apk.fs.*
import gay.pizza.pkg.io.fsPath

class ApkCommand : CliktCommand(help = "Alpine Package Keeper", name = "apk", invokeWithoutSubcommand = true) {
  val repositoryFilePath by option("--repository-file-path", "-R", help = "Repository File Path").fsPath().required()
  val packageCachePath by option("--package-cache-path", "-C", help = "Package Cache Path").fsPath().required()
  val installedDatabasePath by option("--installed-database-path", "-D", help = "Installed Database Path").fsPath().required()
  val worldFilePath by option("--world-file-path", "-W", help = "World File Path").fsPath().required()
  val systemRootPath by option("--root", "-r", help = "System Root Path").fsPath()

  override fun run() {
    val provider = ApkProvider()
    provider.repositoryList = ApkFsRepositoryList(repositoryFilePath)
    provider.packageCache = ApkFsPackageCache(packageCachePath)
    provider.indexCollection = ApkFsIndexCollection(packageCachePath)
    provider.installedDatabase = ApkFsInstalledDatabase(installedDatabasePath)
    provider.world = ApkFsWorld(worldFilePath)
    if (systemRootPath != null) {
      provider.systemRootPath = systemRootPath!!
    }
    currentContext.findOrSetObject { provider }
    currentContext.findOrSetObject { ApkPackageKeeper(provider) }
  }
}
