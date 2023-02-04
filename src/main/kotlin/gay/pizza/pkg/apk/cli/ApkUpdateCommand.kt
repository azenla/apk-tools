package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import gay.pizza.pkg.apk.core.ApkProvider

class ApkUpdateCommand : CliktCommand(help = "Update Package Index", name = "update") {
  val provider by requireObject<ApkProvider>()

  override fun run() {
    provider.keeper.update()
  }
}
