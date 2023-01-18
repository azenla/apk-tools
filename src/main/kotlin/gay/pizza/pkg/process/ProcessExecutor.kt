package gay.pizza.pkg.process

import gay.pizza.pkg.PlatformCurrentWorkingDirectory
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.process.command.ExecutionParameter

interface ProcessExecutor {
  fun execute(job: ExecutionJob): ProcessResult

  fun execute(command: List<ExecutionParameter>, workingDirectoryPath: FsPath = PlatformCurrentWorkingDirectory(), environment: Map<String, String> = mapOf()) =
    execute(ExecutionJob(command, workingDirectoryPath, environment))
}
