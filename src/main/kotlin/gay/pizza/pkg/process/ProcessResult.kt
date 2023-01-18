package gay.pizza.pkg.process

class ProcessResult(
  val exitCode: Int,
  val stdoutBytes: ByteArray,
  val stderrBytes: ByteArray
) {
  val stdoutAsString by lazy {
    stdoutBytes.decodeToString()
  }

  val stderrAsString by lazy {
    stderrBytes.decodeToString()
  }
}
