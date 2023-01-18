package gay.pizza.pkg.apk

class ApkRequirementUnsatisfiedException(val pkg: ApkIndexPackage, val target: ApkIndexRequirementRef) :
  RuntimeException(
    "Package '${pkg.id}' (${pkg.version}) requires '${target.id}' but it is not satisfied by any package."
  )
