package gay.pizza.pkg.apk

class ApkIndexPackageProvide(id: String, val version: String? = null) : ApkIndexRequirementRef(id) {
  companion object {
    fun extract(provide: String): ApkIndexPackageProvide? {
      val parts = provide.split("=", "<", ">", "~")
      val id = parts[0]
      if (id.startsWith("!")) {
        return null
      }
      return ApkIndexPackageProvide(id = id, version = if (parts.size > 1) parts[1] else null)
    }
  }
}
