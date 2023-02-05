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
    for (name in packages) {
      val pkg = provider.index.packageById(name)
      graph.add(pkg)
    }

    if (all) {
      provider.index.packages.forEach { pkg ->
        graph.add(pkg)
      }
    }

    val sorted = graph.simpleOrderSort()
    val total = sorted.size

    val files = mutableListOf<Pair<ApkPackageNode, ApkPackageFile>>()
    for ((i, node) in sorted.withIndex()) {
      val x = i + 1
      val pkg = node.pkg
      println("[${x}/${total}] Fetching ${pkg.id} (${pkg.version})")
      val file = provider.keeper.download(listOf(pkg)).single()
      files.add(node to file)
    }
  }
}
