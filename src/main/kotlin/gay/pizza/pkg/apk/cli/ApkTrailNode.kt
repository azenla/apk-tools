package gay.pizza.pkg.apk.cli

import gay.pizza.pkg.apk.graph.ApkPackageNode

class ApkTrailNode(
  val node: ApkPackageNode? = null,
  var next: ApkTrailNode? = null,
  var root: ApkTrailNode? = null
)
