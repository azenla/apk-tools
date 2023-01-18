package gay.pizza.pkg.process.command

import gay.pizza.pkg.io.FsPath

class SubCommandName(val subcommand: String): ExecutionParameter() {
  override fun toCommandArgument(workingDirectoryPath: FsPath): String = subcommand
}
