package gay.pizza.pkg.apk.index

class ApkIndexDependency(id: String, invert: Boolean = false, val version: ApkVersionSpec? = null) :
  ApkIndexRequirementRef(id, invert) {
  companion object {
    fun extract(dependency: String): ApkIndexDependency {
      var versionOp: String? = null
      var version: String? = null
      var id: String? = null
      for (op in ApkVersionSpec.supportedOperators) {
        val index = dependency.indexOf(op)
        if (index < 0) {
          continue
        }
        versionOp = op
        id = dependency.substring(0, index)
        version = dependency.substring(index + op.length)
        break
      }

      if (id == null) {
        id = dependency
      }

      var invert = false
      if (id.startsWith("!")) {
        invert = true
        id = id.substring(1)
      }

      val versionSpec = if (versionOp != null && version != null) {
        ApkVersionSpec(op = versionOp, version = version)
      } else null

      return ApkIndexDependency(id = id, version = versionSpec, invert = invert)
    }
  }
}
