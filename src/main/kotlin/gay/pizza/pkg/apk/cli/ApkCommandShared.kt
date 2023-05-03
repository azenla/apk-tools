package gay.pizza.pkg.apk.cli

import gay.pizza.pkg.apk.core.ApkProvider
import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.apk.graph.ApkPackageGraph
import gay.pizza.pkg.apk.graph.ApkPackageNode

object ApkCommandShared {
  fun fetch(graph: ApkPackageGraph, provider: ApkProvider): ApkFetchResult {
    val sorted = graph.simpleOrderSort()
    val total = sorted.size

    val files = mutableListOf<Pair<ApkPackageNode, ApkPackageFile>>()
    for ((i, node) in sorted.withIndex()) {
      val x = i + 1
      val pkg = node.pkg
      println("[${x}/${total}] Fetching ${pkg.id} (${pkg.version})")
      val file = provider.keeper.download(listOf(pkg)).single()
      files.add(node to file)
    }
    return ApkFetchResult(files, total)
  }

  class ApkFetchResult(
    val files: MutableList<Pair<ApkPackageNode, ApkPackageFile>>,
    val total: Int
  )
}
