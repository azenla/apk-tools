package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import gay.pizza.pkg.apk.frontend.ApkPackageKeeper

class ApkInspectInstalledCommand : CliktCommand(help = "Inspect Installed Database", name = "inspect-installed") {
  val keeper by requireObject<ApkPackageKeeper>()

  override fun run() {
    val index = keeper.provider.installedDatabase.read()
    for (entry in index.packages) {
      val modifications = entry.modifications()
      for (mod in modifications) {
        println("${entry.lookup("P")} $mod")
      }
    }
  }
}
