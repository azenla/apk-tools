package gay.pizza.pkg.apk.index

open class ApkIndexRequirementRef(val id: String, val invert: Boolean = false) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    other as ApkIndexRequirementRef
    if (id != other.id && !invert) return false
    return true
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
