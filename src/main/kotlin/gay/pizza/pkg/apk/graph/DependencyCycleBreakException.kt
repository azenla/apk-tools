package gay.pizza.pkg.apk.graph

class DependencyCycleBreakException(val node: ApkPackageNode, val dependency: ApkPackageNode) :
  RuntimeException("Breaking dependency cycle where ${node.pkg.id} depends on ${dependency.pkg.id}")
