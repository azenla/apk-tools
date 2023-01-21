package gay.pizza.pkg.apk.index

class ApkIndexPackageDependency(id: String, val version: String? = null) : ApkIndexRequirementRef(id) {
  companion object {
    fun extract(dependency: String): ApkIndexPackageDependency? {
      val parts = dependency.split("=", "<", ">", "~")
      val id = parts[0]
      if (id.startsWith("!")) {
        return null
      }
      return ApkIndexPackageDependency(id = id, version = if (parts.size > 1) parts[1] else null)
    }
  }
}
