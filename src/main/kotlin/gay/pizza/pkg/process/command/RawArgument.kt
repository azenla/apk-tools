package gay.pizza.pkg.process.command

import gay.pizza.pkg.io.FsPath

class RawArgument(val argument: String): ExecutionParameter() {
  override fun toCommandArgument(workingDirectoryPath: FsPath): String = argument
}
