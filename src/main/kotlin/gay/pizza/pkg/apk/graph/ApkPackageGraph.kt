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

  fun add(pkg: ApkIndexPackage, filter: (ApkPackageNode) -> Boolean = { true }) {
    if (seen.contains(pkg)) {
      return
    }
    seen.add(pkg)

    val local = node(pkg)

    val specs = mutableSetOf<ApkIndexRequirementRef>()
    specs.addAll(pkg.dependencies)

    for (spec in specs) {
      if (spec.invert) {
        continue
      }
      val possibleSatisfactions = indexResolution.requirementToProvides[spec]
        ?: throw ApkRequirementUnsatisfiedException(pkg, spec)
      val chosen = possibleSatisfactions.maxBy { it.providerPriority ?: -50 }
      val child = local.addChild(chosen)
      if (filter(local)) {
        edges.add(local to child)
      }
      add(chosen, filter)
    }
  }

  fun addAll(packages: Iterable<ApkIndexPackage>, filter: (ApkPackageNode) -> Boolean = { true }) {
    for (pkg in packages) {
      add(pkg, filter)
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

  fun simpleOrderSort(healCycle: Boolean = true): List<ApkPackageNode> {
    val stack = mutableListOf<ApkPackageNode>()
    val resolving = mutableSetOf<ApkPackageNode>()
    val visited = mutableSetOf<ApkPackageNode>()
    val ignoring = mutableSetOf<ApkPackageNode>()
    for (node in shallowIsolates) {
      simpleOrderSort(node, stack, resolving, visited, ignoring, healCycle)
    }
    return stack
  }

  private fun simpleOrderSort(node: ApkPackageNode,
                              stack: MutableList<ApkPackageNode>,
                              resolving: MutableSet<ApkPackageNode>,
                              visited: MutableSet<ApkPackageNode>,
                              ignoring: MutableSet<ApkPackageNode>,
                              healCycle: Boolean) {
    for (dependency in node.children) {
      if (ignoring.contains(dependency)) {
        continue
      }

      if (resolving.contains(dependency)) {
        GlobalLogger.warn("Cyclic dependency detected on ${dependency.pkg.id} (by ${node.pkg.id}), breaking cycle.")
        throw DependencyCycleBreakException(node, dependency)
      }

      if (!visited.contains(dependency)) {
        resolving.add(dependency)
        try {
          simpleOrderSort(dependency, stack, resolving, visited, ignoring, healCycle)
        } catch (e: DependencyCycleBreakException) {
          if (!healCycle) {
            throw e
          }
          stack.add(dependency)
          ignoring.add(dependency)
          simpleOrderSort(dependency, stack, resolving, visited, ignoring, healCycle)
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

  fun findDependencyCycle(node: ApkPackageNode): Pair<ApkPackageNode, ApkPackageNode>? {
    val resolving = mutableSetOf<ApkPackageNode>()
    val visited = mutableSetOf<ApkPackageNode>()
    return findDependencyCycle(node, resolving, visited)
  }

  fun findDependencyCycle(node: ApkPackageNode, resolving: MutableSet<ApkPackageNode>, visited: MutableSet<ApkPackageNode>): Pair<ApkPackageNode, ApkPackageNode>? {
    for (dependency in node.children) {
      if (resolving.contains(dependency)) {
        return node to dependency
      }

      if (!visited.contains(dependency)) {
        resolving.add(dependency)
        val cycle = findDependencyCycle(dependency, resolving, visited)
        if (cycle != null) {
          return cycle
        }
        resolving.remove(dependency)
        visited.add(dependency)
      }
    }
    return null
  }

  fun parallelOrderSort(breakCycles: Boolean = true): List<List<ApkPackageNode>> {
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
      var set = working.entries
        .filter { (_, children) -> children.isEmpty() }
        .map { (node, _) -> node }
      // If nothing was satisfied this loop, check for cycles.
      // If no cycles exist and the working set is empty,
      // the resolution is complete, and we can break the loop.
      if (set.isEmpty()) {
        if (working.isEmpty()) {
          break
        }

        // Calculate all dependency cycles.
        // Distinct is used here to be sure duplicate cycles
        // which are found via multiple paths are de-duped.
        val cycles = working.keys.mapNotNull { node -> findDependencyCycle(node) }.distinct()

        // If cycle breaking isn't enabled, we should throw an exception.
        if (!breakCycles) {
          val cycleMessages = cycles.map { (node, dependency) -> "${node.pkg.id} -> ${dependency.pkg.id}" }
          throw ApkCyclicDependencyException("Dependency cycles found:\n${cycleMessages.joinToString("\n")}")
        }

        // Break cycles by setting the new resolution set to
        // the dependencies that cycled.
        set = cycles.map { (_, dependency) -> dependency }
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

}
