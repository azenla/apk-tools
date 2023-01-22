package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import gay.pizza.pkg.apk.frontend.ApkPackageKeeper

class ApkUpdateCommand : CliktCommand(help = "Update Package Index", name = "update") {
  val keeper by requireObject<ApkPackageKeeper>()

  override fun run() {
    keeper.update()
  }
}
