package gay.pizza.pkg.apk

class ApkDependencyTree(val graph: ApkIndexGraph, val root: ApkIndexPackage) {
  private val directed = mutableMapOf<ApkIndexPackage, ApkDependencyType>()

  val dependencies: Set<ApkIndexPackage>
    get() = directed.keys.toSet()

  val packages: Set<ApkIndexPackage>
    get() = mutableSetOf<ApkIndexPackage>().apply {
      add(root)
      addAll(dependencies)
    }

  init {
    resolve(root, direct = true, mutableSetOf())
  }

  private fun resolve(pkg: ApkIndexPackage, direct: Boolean, seen: MutableSet<ApkIndexPackage>) {
    if (seen.contains(pkg)) {
      return
    }
    seen.add(pkg)

    val specs = mutableSetOf<ApkIndexRequirementRef>()
    specs.addAll(pkg.dependencies)

    for (spec in specs) {
      val possibleSatisfactions = graph.requirementToProvides[spec]
        ?: throw ApkRequirementUnsatisfiedException(pkg, spec)
      val chosen = possibleSatisfactions.maxBy { it.providerPriority ?: -50 }
      directed.putIfAbsent(chosen, if (direct) ApkDependencyType.Direct else ApkDependencyType.Indirect)
      resolve(chosen, direct = false, seen = seen)
    }
  }

  fun typeOf(pkg: ApkIndexPackage): ApkDependencyType? = directed[pkg]
}
