package gay.pizza.pkg.apk.index

open class ApkIndexRequirementRef(val id: String, val invert: Boolean = false) {
  fun satisfiedBy(other: ApkIndexPackage): Boolean = true

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    other as ApkIndexRequirementRef
    return !(id != other.id && !invert)
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
