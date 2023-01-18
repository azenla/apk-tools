package gay.pizza.pkg.concurrent

class LoopTaskPool : TaskPool {
  private val tasks = mutableListOf<() -> Unit>()

  override fun submit(task: () -> Unit) {
    tasks.add(task)
  }

  override fun await() {
    while (tasks.isNotEmpty()) {
      val task = tasks.removeAt(0)
      task()
    }
  }

  override fun close() {}
}
