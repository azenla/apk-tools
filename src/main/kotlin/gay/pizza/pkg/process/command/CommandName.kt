package gay.pizza.pkg.process.command

import gay.pizza.pkg.io.FsPath

class CommandName(val command: String): ExecutionParameter() {
  override fun toCommandArgument(workingDirectoryPath: FsPath): String = command
}
