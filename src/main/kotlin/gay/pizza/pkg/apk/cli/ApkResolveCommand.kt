package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import gay.pizza.pkg.apk.core.ApkProvider
import gay.pizza.pkg.apk.graph.ApkPackageGraph
import gay.pizza.pkg.apk.graph.ApkPackageNode
import gay.pizza.pkg.apk.index.ApkIndexResolution
import gay.pizza.pkg.log.GlobalLogger

class ApkResolveCommand : CliktCommand(help = "Resolve Dependency Graph", name = "resolve") {
  val packages by argument("package").multiple()
  val provider by requireObject<ApkProvider>()

  val justEdges by option("--just-edges", help = "Show Just Edges").flag()
  val depthFirstSearch by option("--depth-first", help = "Depth First Crawl").flag()
  val dotGraph by option("--dot-graph", help = "Output Dot Graph").flag()
  val installOrder by option("--install-order", help = "Show Install Order").flag()
  val parallelInstallOrder by option("--parallel-install-order", help = "Show Parallel Install Order").flag()
  val parallelInstallOrderDotGraph by option("--parallel-install-order-dot-graph", help = "Show Parallel Install Order Dot Graph").flag()
  val annotatedParallelInstallOrder by option("--annotated-parallel-install-order", help = "Show Annotated Parallel Install Order").flag()

  val trails by option("--trails", help = "Calculate Package Trails").flag()
  val trailsUseDepth by option("--trails-use-depth", help = "Package Trails Use Depth").flag()
  val validateSoundGraph by option("--validate-sound-graph", help = "Validate Sound Graph").flag()
  val onlyPrintStats by option("--just-stats", help = "Just Print Statistics").flag()

  val stress by option("--stress", help = "Stress Resolver").flag()
  val all by option("--all", help = "All Packages").flag()

  val shell by option("--shell", help = "Graph Query Shell").flag()

  fun buildResolutionAndGraph(): Pair<ApkIndexResolution, ApkPackageGraph> {
    val resolution = ApkIndexResolution(provider.index)
    if (validateSoundGraph) {
      resolution.validateSoundGraph(warn = true)
    }

    val graph = ApkPackageGraph(resolution)

    if (all) {
      graph.addAll(provider.index.packages)
    }

    val indexPackages = packages.map { provider.index.packageById(it) }
    graph.addAll(indexPackages)
    return resolution to graph
  }

  override fun run() {
    if (stress) {
      while (true) {
        val (_, graph) = GlobalLogger.timed("computing resolution and graph") {
          buildResolutionAndGraph()
        }

        GlobalLogger.timed("simple install order sort") {
          graph.simpleOrderSort()
        }

        System.gc()
      }
    }
    val (resolution, graph) = buildResolutionAndGraph()

    if (installOrder) {
      installOrder(graph)
      return
    }

    if (parallelInstallOrder) {
      parallelInstallOrder(graph)
      return
    }

    if (annotatedParallelInstallOrder) {
      annotatedParallelInstallOrder(graph)
      return
    }

    if (dotGraph) {
      dotGraph(graph)
      return
    }

    if (parallelInstallOrderDotGraph) {
      parallelInstallDotGraph(graph)
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

    crawl(graph)
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

  private fun annotatedParallelInstallOrder(graph: ApkPackageGraph) {
    val installation = graph.annotatedParallelOrderSort()
    for (set in installation) {
      println(set.joinToString(" ") { item -> "${item.first.pkg.id} [${item.second.joinToString(" ") { it.pkg.id }}]" })
    }
  }

  private fun dotGraph(graph: ApkPackageGraph) {
    println("digraph D {")
    for (edge in graph.edges) {
      println("  \"${edge.first.pkg.id}\" -> \"${edge.second.pkg.id}\"")
    }
    println("}")
  }

  private fun parallelInstallDotGraph(graph: ApkPackageGraph) {
    println("graph D {")
    val installation = graph.parallelOrderSort()
    for (set in installation) {
      var previous: ApkPackageNode? = null
      for (item in set) {
        if (previous != null) {
          println("  \"${previous.pkg.id}\" -- \"${item.pkg.id}\"")
        }
        previous = item
      }

      if (set.size == 1) {
        println("  \"${set.first().pkg.id}\"")
      }
    }
    println("}")
  }

  private fun edges(graph: ApkPackageGraph) {
    for (edge in graph.edges) {
      println("${edge.first.pkg.id} -> ${edge.second.pkg.id}")
    }
  }

  private fun trails(graph: ApkPackageGraph) {
    graph.trails(depth = trailsUseDepth) { trail ->
      println(trail.joinToString(" ") { it?.pkg?.id ?: "CYCLE" })
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
        "annotated-parallel-install-order" -> annotatedParallelInstallOrder(graph)
        "crawl" -> crawl(graph)
        "stats" -> stats(graph)
        "trails" -> trails(graph)
        "edges" -> edges(graph)
        "dot-graph" -> dotGraph(graph)
        "validate-index-graph" -> resolution.validateSoundGraph(warn = true)
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
