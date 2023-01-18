package gay.pizza.pkg.process.command.analysis

import gay.pizza.pkg.io.FsPath

class ExecutionAnalysis(
  val requiredFilePaths: List<FsPath>,
  val requiredDirectoryPaths: List<FsPath>,
  val requiredEntityPaths: List<FsPath>,
  val requiredCommandNames: List<String>,
  val requiredSubCommandPatterns: List<List<String>>
)
