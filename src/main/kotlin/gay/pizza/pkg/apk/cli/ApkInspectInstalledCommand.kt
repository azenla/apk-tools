package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import gay.pizza.pkg.apk.core.ApkProvider

class ApkInspectInstalledCommand : CliktCommand(help = "Inspect Installed Database", name = "inspect-installed") {
  val provider by requireObject<ApkProvider>()

  override fun run() {
    val index = provider.installedDatabase.read()
    for (entry in index.packages) {
      val modifications = entry.modifications()
      for (mod in modifications) {
        println("${entry.lookup("P")} $mod")
      }
    }
  }
}
