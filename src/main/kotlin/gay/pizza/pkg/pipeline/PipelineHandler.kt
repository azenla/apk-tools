package gay.pizza.pkg.pipeline

fun interface PipelineHandler<T> {
  fun handle(value: T)
}
