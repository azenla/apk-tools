package gay.pizza.pkg.process.command

import gay.pizza.pkg.io.FsPath

class JoinedParameter(val left: ExecutionParameter, val right: ExecutionParameter): ExecutionParameter() {
  override fun toCommandArgument(workingDirectoryPath: FsPath): String =
    "${left.toCommandArgument(workingDirectoryPath)}${right.toCommandArgument(workingDirectoryPath)}"

  override fun listSubParameters(): List<ExecutionParameter> = listOf(left, right)
}
