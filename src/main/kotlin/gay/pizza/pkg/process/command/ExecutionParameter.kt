package gay.pizza.pkg.process.command

import gay.pizza.pkg.PlatformPath
import gay.pizza.pkg.io.FsPath

abstract class ExecutionParameter {
  abstract fun toCommandArgument(workingDirectoryPath: FsPath): String

  open fun listSubParameters(): List<ExecutionParameter> = emptyList()

  override fun toString(): String {
    return "${this::class.simpleName}(${toCommandArgument(PlatformPath("."))})"
  }
}
