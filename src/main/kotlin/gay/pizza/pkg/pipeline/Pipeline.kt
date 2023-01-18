package gay.pizza.pkg.pipeline

interface Pipeline<T> {
  fun emit(item: T)

  fun handle(handler: PipelineHandler<T>): PipelineHandlerToken<T>
}
