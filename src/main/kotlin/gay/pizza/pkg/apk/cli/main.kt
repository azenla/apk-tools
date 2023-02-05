package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = ApkCommand().subcommands(
  ApkUpdateCommand(),
  ApkResolveCommand(),
  ApkAddCommand(),
  ApkInspectInstalledCommand(),
  ApkDownloadCommand(),
  ApkFsCalculatorCommand(),
  ApkIndexCommand()
).main(args)
