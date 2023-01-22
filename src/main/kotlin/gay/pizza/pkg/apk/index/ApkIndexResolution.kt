package gay.pizza.pkg.apk.index

import gay.pizza.pkg.log.GlobalLogger

class ApkIndexResolution(val index: ApkIndex) {
  private val mutableRequirementToProvides = mutableMapOf<ApkIndexRequirementRef, MutableSet<ApkIndexPackage>>()
  private val installIfsCache = mutableMapOf<ApkIndexRequirementRef, MutableSet<ApkIndexRequirementRef>>()

  val requirementToProvides: Map<ApkIndexRequirementRef, Set<ApkIndexPackage>>
    get() = mutableRequirementToProvides

  init {
    GlobalLogger.timed("computing index resolution cache") {
      for (pkg in index.packages) {
        for (provide in pkg.provides) {
          mutableRequirementToProvides.getOrPut(provide) { mutableSetOf() }.add(pkg)
        }
        mutableRequirementToProvides.getOrPut(pkg) { mutableSetOf() }.add(pkg)
        for (installIfPkg in pkg.installIf) {
          installIfsCache.getOrPut(installIfPkg) { mutableSetOf() }.add(pkg)
        }
      }
    }
  }

  fun validateSoundGraph(warn: Boolean = false) {
    for (pkg in index.packages) {
      for (dependency in pkg.dependencies) {
        val provides = requirementToProvides[dependency]
        if (provides.isNullOrEmpty()) {
          val message = "Package ${pkg.id} has dependency ${dependency.id} that is not satisfied."
          if (warn) {
            GlobalLogger.warn(message)
          } else {
            throw RuntimeException(message)
          }
        }
      }
    }
  }

  fun findInstallIfs(pkg: ApkIndexPackage): Set<ApkIndexRequirementRef> = installIfsCache[pkg] ?: emptySet()
}
