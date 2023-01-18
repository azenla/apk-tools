package gay.pizza.pkg.pipeline

import gay.pizza.pkg.concurrent.TaskPool

class PooledPipeline<T>(private val delegate: Pipeline<T>, val pool: TaskPool): Pipeline<T> by delegate {
  override fun emit(item: T) {
    pool.submit {
      delegate.emit(item)
    }
  }
}
