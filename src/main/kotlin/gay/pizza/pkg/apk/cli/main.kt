package gay.pizza.pkg.apk.cli

import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = ApkCommand().subcommands(
  ApkResolveCommand(),
  ApkAddCommand(),
  ApkInspectInstalledCommand()
).main(args)
