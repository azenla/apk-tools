package gay.pizza.pkg.apk

class ApkPackageNode(val graph: ApkPackageGraph, val pkg: ApkIndexPackage) {
  var parents: MutableSet<ApkPackageNode> = mutableSetOf()
  var children: MutableSet<ApkPackageNode> = mutableSetOf()

  fun addChild(pkg: ApkIndexPackage): ApkPackageNode {
    val node = graph.node(pkg)
    children.add(node)
    node.parents.add(this)
    return node
  }

  fun removeChild(pkg: ApkIndexPackage): ApkPackageNode {
    val node = graph.node(pkg)
    children.remove(node)
    node.parents.remove(this)
    return node
  }

  override fun toString(): String = "node[${pkg}]"
}
