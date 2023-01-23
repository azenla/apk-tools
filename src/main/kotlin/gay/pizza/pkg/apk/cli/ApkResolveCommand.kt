package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import gay.pizza.pkg.apk.graph.ApkPackageNode
import gay.pizza.pkg.apk.index.ApkIndexResolution
import gay.pizza.pkg.apk.graph.ApkPackageGraph
import gay.pizza.pkg.apk.frontend.ApkPackageKeeper

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

  val shell by option("--shell", help = "Graph Query Shell").flag()

  override fun run() {
    val resolution = ApkIndexResolution(keeper.index)
    val graph = ApkPackageGraph(resolution)

    if (validateSoundGraph) {
      graph.indexResolution.validateSoundGraph(warn = true)
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
      installOrder(graph)
      return
    }

    if (parallelInstallOrder) {
      parallelInstallOrder(graph)
      return
    }

    if (dotGraph) {
      return
    }

    if (justEdges) {
      edges(graph)
      return
    }

    if (trails) {
      trails(graph)
      return
    }

    if (onlyPrintStats) {
      stats(graph)
      return
    }

    if (shell) {
      shell(resolution, graph)
      return
    }
  }

  private fun installOrder(graph: ApkPackageGraph) {
    val installation = graph.simpleOrderSort()
    println(installation.joinToString(" ") { it.pkg.id })
  }

  private fun parallelInstallOrder(graph: ApkPackageGraph) {
    val installation = graph.parallelOrderSort()
    for (set in installation) {
      println(set.joinToString(" ") { it.pkg.id })
    }
  }

  private fun dotGraph(graph: ApkPackageGraph) {
    println("digraph D {")
    for (edge in graph.edges) {
      println("  \"${edge.first.pkg.id}\" -> \"${edge.second.pkg.id}\"")
    }
    println("}")
  }

  private fun edges(graph: ApkPackageGraph) {
    for (edge in graph.edges) {
      println("${edge.first.pkg.id} -> ${edge.second.pkg.id}")
    }
  }

  private fun trails(graph: ApkPackageGraph) {
    graph.trails(trailsUseDepth) { trail ->
      println(trail.map { it?.pkg?.id }.joinToString(" ") { it ?: "CYCLE" })
    }
  }

  private fun stats(graph: ApkPackageGraph) {
    println("${graph.nodes.size} nodes")
    println("${graph.edges.size} edges")
  }

  private fun crawl(graph: ApkPackageGraph) {
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

  private fun shell(resolution: ApkIndexResolution, defaultGraph: ApkPackageGraph) {
    var graph = defaultGraph
    val reader = System.`in`.bufferedReader()
    while (true) {
      val line = reader.readLine()
      val parts = line.split(" ")
      when (parts[0]) {
        "install-order" -> installOrder(graph)
        "parallel-install-order" -> parallelInstallOrder(graph)
        "crawl" -> crawl(graph)
        "stats" -> stats(graph)
        "trails" -> trails(graph)
        "edges" -> edges(graph)
        "dot-graph" -> dotGraph(graph)
        "validate-index-graph" -> graph.indexResolution.validateSoundGraph(warn = true)
        "add" -> parts.drop(1).forEach { id -> graph.add(graph.indexResolution.index.packageById(id)) }
        "all" -> {
          for (pkg in graph.indexResolution.index.packages) {
            graph.add(pkg)
          }
        }
        "reset" -> graph = ApkPackageGraph(resolution)
      }
    }
  }
}
