package gay.pizza.pkg.concurrent

interface TaskPool {
  fun submit(task: () -> Unit)
  fun await()
  fun close()
}
