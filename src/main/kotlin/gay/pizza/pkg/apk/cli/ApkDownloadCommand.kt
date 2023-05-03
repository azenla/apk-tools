package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import gay.pizza.pkg.apk.core.ApkProvider
import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.apk.graph.ApkPackageNode

class ApkDownloadCommand : CliktCommand(help = "Download Packages", name = "download") {
  val packages by argument("package").multiple()
  val provider by requireObject<ApkProvider>()

  val all by option("--all", help = "All Packages").flag()

  override fun run() {
    val graph = provider.createPackageGraph()

    val indexPackages = packages.map { provider.index.packageById(it) }
    graph.addAll(indexPackages)

    if (all) {
      graph.addAll(provider.index.packages)
    }

    ApkCommandShared.fetch(graph, provider)
  }
}
