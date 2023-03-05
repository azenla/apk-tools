package gay.pizza.pkg.apk.index

class ApkVersionSpec(
  val op: String = "=",
  val version: String
) {
  companion object {
    val supportedOperators = listOf(
      "<=",
      ">=",
      "=",
      "<",
      ">",
      "~"
    )
  }

  override fun toString(): String = "${op}${version}"
  override fun hashCode(): Int = "${op}${version}".hashCode()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    other as ApkVersionSpec
    if (op != other.op) return false
    if (version != other.version) return false
    return true
  }
}
