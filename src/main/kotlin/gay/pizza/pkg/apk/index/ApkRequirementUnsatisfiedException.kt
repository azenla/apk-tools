package gay.pizza.pkg.apk.index

import gay.pizza.pkg.apk.index.ApkIndexPackage
import gay.pizza.pkg.apk.index.ApkIndexRequirementRef

class ApkRequirementUnsatisfiedException(val pkg: ApkIndexPackage, val target: ApkIndexRequirementRef) :
  RuntimeException(
    "Package '${pkg.id}' (${pkg.version}) requires '${target.id}' but it is not satisfied by any package."
  )
