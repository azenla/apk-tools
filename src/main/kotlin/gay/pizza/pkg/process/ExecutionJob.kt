package gay.pizza.pkg.process

import gay.pizza.pkg.process.command.analysis.ExecutionAnalyzer
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.process.command.ExecutionParameter
import gay.pizza.pkg.process.command.analysis.ExecutionAnalysis

class ExecutionJob(
  val command: List<ExecutionParameter>,
  val workingDirectoryPath: FsPath,
  val environment: Map<String, String>
) {
  fun expandCommandArguments(): List<String> = command.map { parameter ->
    parameter.toCommandArgument(workingDirectoryPath)
  }

  fun expandSubParameters(): List<ExecutionParameter> {
    val all = mutableListOf<ExecutionParameter>()
    for (parameter in command) {
      all.add(parameter)
      all.addAll(parameter.listSubParameters())
    }
    return all
  }

  fun analyze(): ExecutionAnalysis = ExecutionAnalyzer(this).analyze()
}
