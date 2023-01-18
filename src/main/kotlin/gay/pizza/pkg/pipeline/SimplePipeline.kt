package gay.pizza.pkg.pipeline

class SimplePipeline<T> : Pipeline<T> {
  private val handlers = mutableListOf<PipelineHandler<T>>()

  override fun emit(item: T) {
    for (handler in handlers) {
      handler.handle(item)
    }
  }

  override fun handle(handler: PipelineHandler<T>): PipelineHandlerToken<T> {
    handlers.add(handler)
    return SimpleToken(this, handler)
  }

  private class SimpleToken<T>(val pipeline: SimplePipeline<T>, val handler: PipelineHandler<T>) : PipelineHandlerToken<T> {
    override fun cancel() {
      pipeline.handlers.removeAll { it == handler }
    }
  }
}
