package gay.pizza.pkg.process.command

import gay.pizza.pkg.io.FsPath

class RelativeFilePath(val path: FsPath): ExecutionParameter() {
  override fun toCommandArgument(workingDirectoryPath: FsPath): String {
    return path.relativeTo(workingDirectoryPath).fullPathString
  }
}
