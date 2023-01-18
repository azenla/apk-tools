package gay.pizza.pkg.concurrent.java

import gay.pizza.pkg.concurrent.TaskPool
import java.util.concurrent.ScheduledThreadPoolExecutor

class JavaTaskPool(concurrency: Int) : TaskPool {
  private val pool = ScheduledThreadPoolExecutor(concurrency) { task ->
    Thread(task).apply {
      isDaemon = true
    }
  }

  override fun submit(task: () -> Unit) {
    pool.submit(task)
  }

  override fun await() {
    while (pool.activeCount != 0) {
      Thread.yield()
    }
  }

  override fun close() {
    pool.shutdown()
  }
}
