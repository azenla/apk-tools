package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import gay.pizza.pkg.PlatformTaskPool
import gay.pizza.pkg.apk.graph.ApkPackageNode
import gay.pizza.pkg.apk.index.ApkIndexResolution
import gay.pizza.pkg.apk.graph.ApkPackageGraph
import gay.pizza.pkg.apk.frontend.ApkPackageKeeper
import java.util.concurrent.atomic.AtomicInteger

class ApkResolveCommand : CliktCommand(help = "Resolve Dependency Graph", name = "resolve") {
  val packages by argument("package").multiple()
  val keeper by requireObject<ApkPackageKeeper>()

  val justEdges by option("--just-edges", help = "Show Just Edges").flag()
  val depthFirstSearch by option("--depth-first", help = "Depth First Crawl").flag()
  val dotGraph by option("--dot-graph", help = "Output Dot Graph").flag()
  val installOrder by option("--install-order", help = "Show Install Order").flag()
  val parallelInstallOrder by option("--parallel-install-order", help = "Show Parallel Install Order").flag()
  val trails by option("--trails", help = "Calculate Package Trails").flag()
  val trailsUseDepth by option("--trails-use-depth", help = "Package Trails Use Depth").flag()
  val validateSoundGraph by option("--validate-sound-graph", help = "Validate Sound Graph").flag()
  val onlyPrintStats by option("--just-stats", help = "Just Print Statistics").flag()

  override fun run() {
    val resolution = ApkIndexResolution(keeper.index)
    val graph = ApkPackageGraph(resolution)

    if (validateSoundGraph) {
      graph.indexGraph.validateSoundGraph(warn = true)
    }

    for (name in packages) {
      if (name == ":all") {
        keeper.index.packages.forEach { graph.add(it) }
        continue
      }
      val pkg = keeper.index.packageById(name)
      graph.add(pkg)
    }

    if (installOrder) {
      val installation = graph.simpleOrderSort()
      for ((index, node) in installation.withIndex()) {
        println("$index ${node.pkg.id}")
      }
      return
    }

    if (parallelInstallOrder) {
      val installation = graph.parallelOrderSort()
      for (set in installation) {
        println(set.joinToString(" ") { it.pkg.id })
      }
      return
    }

    if (dotGraph) {
      println("digraph D {")
      for (edge in graph.edges) {
        println("  \"${edge.first.pkg.id}\" -> \"${edge.second.pkg.id}\"")
      }
      println("}")
      return
    }

    if (justEdges) {
      for (edge in graph.edges) {
        println("${edge.first.pkg.id} -> ${edge.second.pkg.id}")
      }
      return
    }

    if (trails) {
      graph.trails(trailsUseDepth) { trail ->
        println(trail.map { it?.pkg?.id }.joinToString(" ") { it ?: "CYCLE" })
      }
      return
    }

    if (onlyPrintStats) {
      println("${graph.nodes.size} nodes")
      println("${graph.edges.size} edges")
      var trailCount = 0
      graph.trails { trailCount++ }
      println("$trailCount trails")
      return
    }

    val seen = mutableSetOf<ApkPackageNode>()
    for (isolate in if (depthFirstSearch) graph.deepIsolates else graph.shallowIsolates) {
      crawler(seen, isolate, !depthFirstSearch, 0)
    }
  }

  private fun crawler(seen: MutableSet<ApkPackageNode>, node: ApkPackageNode, shallow: Boolean, level: Int) {
    if (seen.contains(node)) {
      println(("  ".repeat(level)) + "-> " + node.pkg.id)
      return
    }
    seen.add(node)
    println(("  ".repeat(level)) + "-> " + node.pkg.id)
    (if (shallow) node.children else node.parents).forEach {
      crawler(seen, it, shallow, level + 1)
    }
  }
}
