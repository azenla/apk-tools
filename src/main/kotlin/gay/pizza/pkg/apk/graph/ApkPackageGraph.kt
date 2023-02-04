package gay.pizza.pkg.apk.graph

import gay.pizza.pkg.apk.index.ApkIndexPackage
import gay.pizza.pkg.apk.index.ApkIndexRequirementRef
import gay.pizza.pkg.apk.index.ApkIndexResolution
import gay.pizza.pkg.apk.index.ApkRequirementUnsatisfiedException
import gay.pizza.pkg.log.GlobalLogger

class ApkPackageGraph(val indexResolution: ApkIndexResolution) {
  private val seen = mutableSetOf<ApkIndexPackage>()
  private val allNodes = mutableMapOf<ApkIndexPackage, ApkPackageNode>()

  val nodes: Map<ApkIndexPackage, ApkPackageNode>
    get() = allNodes

  val edges = mutableSetOf<Pair<ApkPackageNode, ApkPackageNode>>()

  val shallowIsolates: Sequence<ApkPackageNode>
    get() = allNodes.values.asSequence().filter { it.parents.isEmpty() }

  val deepIsolates: Sequence<ApkPackageNode>
    get() = allNodes.values.asSequence().filter { it.children.isEmpty() }

  fun node(pkg: ApkIndexPackage): ApkPackageNode =
    allNodes.getOrPut(pkg) { ApkPackageNode(this, pkg) }

  fun add(pkg: ApkIndexPackage) {
    if (seen.contains(pkg)) {
      return
    }
    seen.add(pkg)

    val specs = mutableSetOf<ApkIndexRequirementRef>()
    specs.addAll(pkg.dependencies)

    val local = node(pkg)
    for (spec in specs) {
      val possibleSatisfactions = indexResolution.requirementToProvides[spec]
        ?: throw ApkRequirementUnsatisfiedException(pkg, spec)
      val chosen = possibleSatisfactions.maxBy { it.providerPriority ?: -50 }
      val child = local.addChild(chosen)
      edges.add(local to child)
      add(chosen)
    }
  }

  fun trails(start: ApkPackageNode? = null, depth: Boolean = false): List<List<ApkPackageNode?>> {
    val results = mutableListOf<List<ApkPackageNode?>>()
    trails(start, depth = depth) { results.add(it) }
    return results
  }

  fun trails(start: ApkPackageNode? = null, depth: Boolean = false, handler: (List<ApkPackageNode?>) -> Unit) {
    fun crawl(node: ApkPackageNode, trail: List<ApkPackageNode?>) {
      if (trail.contains(node)) {
        val resulting = trail.toMutableList().apply {
          add(node)
          add(null)
        }
        handler(resulting)
        return
      }
      val resulting = trail.toMutableList().apply { add(node) }
      val next = if (depth) node.parents else node.children
      if (next.isNotEmpty()) {
        for (item in next) {
          crawl(item, resulting)
        }
      } else {
        handler(resulting)
      }
    }

    if (start != null) {
      crawl(start, emptyList())
    } else {
      for (item in if (depth) deepIsolates else shallowIsolates) {
        crawl(item, emptyList())
      }
    }
  }

  fun clone(): ApkPackageGraph {
    val copy = ApkPackageGraph(indexResolution)
    for (pkg in allNodes.keys) {
      copy.node(pkg)
    }

    for (node in allNodes.values) {
      for (child in node.children) {
        copy.node(node.pkg).addChild(child.pkg)
      }
    }
    return copy
  }

  fun simpleOrderSort(): List<ApkPackageNode> {
    val stack = mutableListOf<ApkPackageNode>()
    val resolving = mutableSetOf<ApkPackageNode>()
    val visited = mutableSetOf<ApkPackageNode>()
    val ignoring = mutableSetOf<ApkPackageNode>()
    for (node in shallowIsolates) {
      simpleOrderSort(node, stack, resolving, visited, ignoring)
    }
    return stack
  }

  private fun simpleOrderSort(node: ApkPackageNode,
                              stack: MutableList<ApkPackageNode>,
                              resolving: MutableSet<ApkPackageNode>,
                              visited: MutableSet<ApkPackageNode>,
                              ignoring: MutableSet<ApkPackageNode>) {
    for (dependency in node.children) {
      if (ignoring.contains(dependency)) {
        continue
      }

      if (resolving.contains(dependency)) {
        GlobalLogger.warn("Cyclic dependency detected on ${dependency.pkg.id} (by ${node.pkg.id}), breaking cycle.")
        throw DependencyCycleBreakException
      }

      if (!visited.contains(dependency)) {
        resolving.add(dependency)
        try {
          simpleOrderSort(dependency, stack, resolving, visited, ignoring)
        } catch (e: DependencyCycleBreakException) {
          stack.add(dependency)
          ignoring.add(dependency)
          simpleOrderSort(dependency, stack, resolving, visited, ignoring)
          ignoring.remove(dependency)
        }
        resolving.remove(dependency)
        visited.add(dependency)
      }
    }

    if (!stack.contains(node)) {
      stack.add(node)
    }
  }

  fun parallelOrderSort(): List<List<ApkPackageNode>> {
    var data = nodes.mapKeys { node(it.key) }.mapValues {
      it.value.children.toMutableSet()
    }.toMutableMap()

    data.forEach { entry ->
      entry.value.remove(entry.key)
    }

    val extraItems = data.values.reduce { a, b ->
      a.union(b).toMutableSet()
    } - data.keys.toSet()
    data.putAll(extraItems.map { it to mutableSetOf() })
    val result = mutableListOf<List<ApkPackageNode>>()
    mloop@ while (true) {
      iloop@ while (true) {
        val ordered = data.filter { (_, v) -> v.isEmpty() }.map { (k, _) -> k }
        if (ordered.isEmpty()) {
          break@iloop
        }

        result.add(ordered)
        data = data.filter { (k, _) -> !ordered.contains(k) }
          .map { (k, v) -> v.removeAll(ordered.toSet()); k to v }
          .toMap()
          .toMutableMap()
      }

      if (data.isNotEmpty()) {
        throw ApkCyclicDependencyException("Cyclic dependency exists:\n${data.map { entry -> entry.value.joinToString(", ") { it.pkg.id } }.joinToString("\n")}")
      } else {
        break@mloop
      }
    }
    return result
  }

  fun flexibleOrderSort(): List<List<ApkPackageNode>> = try {
    parallelOrderSort()
  } catch (e: ApkCyclicDependencyException) {
    simpleOrderSort().map { listOf(it) }
  }

  private object DependencyCycleBreakException : RuntimeException("Breaking Dependency Cycle")
}
