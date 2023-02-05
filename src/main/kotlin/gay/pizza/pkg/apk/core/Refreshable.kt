package gay.pizza.pkg.apk.core

import kotlin.reflect.KProperty

class Refreshable<out V>(val calculate: () -> V) {
  val value: V
    get() = fetch()

  private var internal: V? = null

  private fun fetch(): V {
    if (internal == null) {
      internal = calculate()
    }
    return internal!!
  }

  fun refresh() {
    internal = calculate()
  }

  operator fun <T> getValue(thisRef: T, property: KProperty<*>): V {
    return value
  }
}

fun <V> refreshable(calculate: () -> V) = Refreshable(calculate)
