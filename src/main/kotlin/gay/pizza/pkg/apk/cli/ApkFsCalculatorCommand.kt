package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import gay.pizza.pkg.apk.core.ApkProvider
import gay.pizza.pkg.apk.file.ApkFsCalculator
import gay.pizza.pkg.apk.file.ApkFsEntryType
import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.apk.graph.ApkPackageNode

class ApkFsCalculatorCommand : CliktCommand(help = "FileSystem Calculator", name = "fscalc") {
  val packages by argument("package").multiple()
  val provider by requireObject<ApkProvider>()

  val all by option("--all", help = "All Packages").flag()

  override fun run() {
    val graph = provider.createPackageGraph()
    for (name in packages) {
      val pkg = provider.index.packageById(name)
      graph.add(pkg)
    }

    if (all) {
      provider.index.packages.forEach { pkg ->
        graph.add(pkg)
      }
    }

    val sorted = graph.simpleOrderSort()

    val files = mutableListOf<Pair<ApkPackageNode, ApkPackageFile>>()
    for (node in sorted) {
      val pkg = node.pkg
      val file = provider.packageCache.read(pkg) ?:
        throw RuntimeException("Package ${pkg.id} is not downloaded.")
      files.add(node to file)
    }

    val calculator = ApkFsCalculator()
    for ((_, file) in files) {
      calculator.addPackageFile(file)
    }

    for ((path, entry) in calculator.entries) {
      if (entry.type == ApkFsEntryType.Directory) {
        println("mkdir $path")
      } else {
        println("touch $path")
      }
    }
  }
}
