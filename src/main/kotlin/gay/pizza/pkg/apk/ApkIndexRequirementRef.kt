package gay.pizza.pkg.apk

open class ApkIndexRequirementRef(val id: String) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    other as ApkIndexRequirementRef
    if (id != other.id) return false
    return true
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
