package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import gay.pizza.pkg.apk.*
import gay.pizza.pkg.io.fsPath

class ApkCommand : CliktCommand(help = "Alpine Package Keeper", name = "apk", invokeWithoutSubcommand = true) {
  val repositories by option("--repository", help = "Repository URL").multiple()
  val packageCachePath by option("--package-cache-path", help = "Package Cache Path").fsPath().required()
  val systemRootPath by option("--root", help = "System Root Path").fsPath()

  override fun run() {
    val provider = ApkProvider()
    provider.repositoryList = ApkStaticRepositoryList(repositories)
    provider.packageCache = ApkFsPackageCache(packageCachePath)
    provider.indexCollection = ApkFsIndexCollection(packageCachePath)
    if (systemRootPath != null) {
      provider.systemRootPath = systemRootPath!!
    }
    currentContext.findOrSetObject { provider }
    currentContext.findOrSetObject { ApkPackageKeeper(provider) }
  }
}
