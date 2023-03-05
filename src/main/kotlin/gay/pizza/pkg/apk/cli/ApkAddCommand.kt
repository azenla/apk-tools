package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import gay.pizza.pkg.apk.core.ApkProvider
import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.apk.graph.ApkPackageGraph
import gay.pizza.pkg.apk.graph.ApkPackageNode
import gay.pizza.pkg.apk.index.ApkIndexResolution

class ApkAddCommand : CliktCommand(help = "Add Packages", name = "add") {
  val packages by argument("package").multiple(required = true)
  val provider by requireObject<ApkProvider>()

  override fun run() {
    val resolution = ApkIndexResolution(provider.index)
    val graph = ApkPackageGraph(resolution)

    val startWorldPackages = provider.world.read()

    val indexPackages = packages.map { provider.index.packageById(it) }
    graph.addAll(indexPackages)

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

    val wouldInstallWorld = files.map { it.first.pkg.id }.toList()
    for ((i, pair) in files.withIndex()) {
      val (node, file) = pair
      val x = i + 1
      val pkg = node.pkg
      println("[${x}/${total}] Installing ${pkg.id} (${pkg.version})")
      val info = file.packageInfo().shrink()
      val entry = info.toRawIndexEntry()
      if (entry.lookup("P") != node.pkg.id) {
        throw RuntimeException("Mismatch of package ${node.pkg.id} and file ${file.path.fullPathString} (${entry.lookup("P")})")
      }
      provider.keeper.install(listOf(file))
    }

    val resultingWorld = startWorldPackages.toMutableList()
    wouldInstallWorld.forEach { item -> if (!resultingWorld.contains(item)) { resultingWorld.add(item) } }
    provider.world.write(resultingWorld)
  }
}
