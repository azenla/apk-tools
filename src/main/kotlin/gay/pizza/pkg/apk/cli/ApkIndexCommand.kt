package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import gay.pizza.pkg.apk.file.ApkPackageFile
import gay.pizza.pkg.io.fsPath
import gay.pizza.pkg.io.isRegularFile
import gay.pizza.pkg.io.list

class ApkIndexCommand : CliktCommand(help = "Index Repository", name = "index") {
  val directory by argument("directory").fsPath(mustExist = true, canBeFile = false)

  override fun run() {
    val files = directory.list()
      .filter { path ->
        path.isRegularFile() &&
          path.entityNameString.endsWith(".apk")
      }
      .map { path ->
        ApkPackageFile(path)
      }
      .toList()

    for (file in files) {
      val pkgInfo = file.packageInfo()
      val indexPackage = pkgInfo.toIndexPackage()
      println(indexPackage)
    }
  }
}
