package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import gay.pizza.pkg.apk.core.ApkProvider
import gay.pizza.pkg.apk.file.ApkFsState
import gay.pizza.pkg.apk.file.ApkFsTree
import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.apk.graph.ApkPackageNode

class ApkFsCalculatorCommand : CliktCommand(help = "FileSystem Calculator", name = "fscalc") {
  val packages by argument("package").multiple()
  val provider by requireObject<ApkProvider>()

  val all by option("--all", help = "All Packages").flag()
  val safeExtractionGraph by option("--safe-extraction-graph", help = "Safe Extraction Graph").flag()

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

    val files = mutableListOf<Pair<ApkPackageNode, ApkPackageFile>>()
    val states = mutableMapOf<ApkPackageNode, ApkFsState>()

    for (node in sorted) {
      val pkg = node.pkg
      val file = provider.packageCache.read(pkg) ?:
        throw RuntimeException("Package ${pkg.id} is not downloaded.")
      files.add(node to file)
      val state = ApkFsState()
      state.addPackageFile(file)
      states[node] = state
    }

    for ((_, firstState) in states) {
      for ((_, secondState) in states) {
        if (firstState == secondState) {
          continue
        }

        val sharedState = firstState.commonalities(secondState)
        println(sharedState.entities.keys.toList())
      }
    }

    val tree = ApkFsTree()
    states.values.forEach { state -> tree.populate(state) }
    tree.root.crawl { entry ->
      println(entry.entity.path)
    }
  }
}
