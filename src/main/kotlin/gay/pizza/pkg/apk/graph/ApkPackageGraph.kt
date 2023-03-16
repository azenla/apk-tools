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
      if (spec.invert) {
        continue
      }
      val possibleSatisfactions = indexResolution.requirementToProvides[spec]
        ?: throw ApkRequirementUnsatisfiedException(pkg, spec)
      val chosen = possibleSatisfactions.maxBy { it.providerPriority ?: -50 }
      val child = local.addChild(chosen)
      edges.add(local to child)
      add(chosen)
    }
  }

  fun addAll(packages: Iterable<ApkIndexPackage>) {
    for (pkg in packages) {
      add(pkg)
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
    val results = mutableListOf<List<ApkPackageNode>>()

    // Create a mutable map of all nodes to all of their children in a set.
    var working = nodes.mapKeys { node(it.key) }.mapValues {
      it.value.children.toMutableSet()
    }.toMutableMap()

    // Remove any self dependencies (node that depends on itself)
    working.forEach { entry ->
      entry.value.remove(entry.key)
    }

    // Collect all child nodes that are not already in the node map.
    // This should be empty every time in this code.
    val extraNodes = working.values.reduce { a, b ->
      a.union(b).toMutableSet()
    } - working.keys.toSet()

    assert(extraNodes.isEmpty())

    // Put all the extra nodes into the working set, mapped to
    // an empty mutable set.
    working.putAll(extraNodes.map { it to mutableSetOf() })

    while (true) {
      // All nodes whose dependencies are now satisfied.
      val set = working.entries
        .filter { (_, children) -> children.isEmpty() }
        .map { (node, _) -> node }
      // If nothing was satisfied this loop, exit the loop.
      // If there are entries to be left, they will be dealt
      // with before the method returns.
      if (set.isEmpty()) {
        break
      }
      // Add this installation set to the list of installation sets.
      results.add(set)

      // Filter the working set for anything that wasn't dealt
      // with this iteration.
      // Create a new map from those entries where the dependencies
      // are freed from anything already handled.
      working = working.filter { (node, _) ->
        !set.contains(node)
      }.map { (node, value) ->
        value.removeAll(set.toSet())
        node to value
      }.toMap().toMutableMap()
    }

    // Check if a dependency cycle was found, which happens when nothing
    // was removed from the working set, yet things are still left over.
    // TODO(azenla): Resolve dependency loop via forced parallel installation.
    if (working.isNotEmpty()) {
      throw ApkCyclicDependencyException(
        "Cyclic dependency exists:\n${working.map { entry -> entry.value.joinToString(", ") { it.pkg.id } }.joinToString("\n")}")
    }
    return results
  }

  fun flexibleOrderSort(): List<List<ApkPackageNode>> = try {
    parallelOrderSort()
  } catch (e: ApkCyclicDependencyException) {
    simpleOrderSort().map { listOf(it) }
  }

  fun annotatedParallelOrderSort(): List<List<Pair<ApkPackageNode, List<ApkPackageNode>>>> {
    val parallel = parallelOrderSort()

    val results = mutableListOf<MutableList<Pair<ApkPackageNode, List<ApkPackageNode>>>>()
    val visited = mutableSetOf<ApkPackageNode>()
    for (set in parallel) {
      val resultSet = mutableListOf<Pair<ApkPackageNode, List<ApkPackageNode>>>()
      for (item in set) {
        val wanted = visited.filter { it.parents.contains(item) }
        resultSet.add(item to wanted)
      }
      results.add(resultSet)
      visited.addAll(set)
    }
    return results
  }

  private object DependencyCycleBreakException : RuntimeException("Breaking Dependency Cycle")
}
