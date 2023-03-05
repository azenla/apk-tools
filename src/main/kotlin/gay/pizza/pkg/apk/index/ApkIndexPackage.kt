package gay.pizza.pkg.apk.index

class ApkIndexPackage(
  val checksum: String,
  id: String,
  val version: String,
  val architecture: String? = null,
  val packageSize: Long,
  val installedSize: Long,
  val description: String,
  val url: String,
  val license: String,
  val origin: String? = null,
  val maintainer: String? = null,
  val buildTime: Long? = null,
  val commit: String? = null,
  val providerPriority: Int? = null,
  val dependencies: List<ApkIndexDependency>,
  val provides: List<ApkIndexPackageProvide>,
  val installIf: List<ApkIndexPackageInstallIf>,
  val raw: ApkRawIndexEntry
) : ApkIndexRequirementRef(id) {
  val downloadFileName: String = "${id}-${version}.apk"

  companion object {
    fun extract(entry: ApkRawIndexEntry): ApkIndexPackage {
      val map = entry.toMap()
      fun required(key: String): String =
        map[key] ?: throw RuntimeException("entry key $key missing (${entry.data})")

      val checksum = required("C")
      val id = required("P")
      val version = required("V")
      val architecture = map["A"]
      val packageSize = required("S").toLong()
      val installedSize = required("I").toLong()
      val description = required("T")
      val url = required("U")
      val license = required("L")
      val origin = map["o"]
      val maintainer = map["m"]
      val buildTime = map["t"]?.toLong()
      val commit = map["c"]
      val providerPriority = map["k"]?.toInt()
      val dependencies = (map["D"] ?: "").split(" ").filter { it.isNotBlank() }
      val provides = (map["p"] ?: "").split(" ").filter { it.isNotBlank() }
      val installIf = (map["i"] ?: "").split(" ").filter { it.isNotBlank() }

      return ApkIndexPackage(
        checksum = checksum,
        id = id,
        version = version,
        architecture = architecture,
        packageSize = packageSize,
        installedSize = installedSize,
        description = description,
        url = url,
        license = license,
        origin = origin,
        maintainer = maintainer,
        buildTime = buildTime,
        commit = commit,
        providerPriority = providerPriority,
        dependencies = dependencies.mapNotNull { dependency -> ApkIndexDependency.extract(dependency) },
        provides = provides.mapNotNull { provide -> ApkIndexPackageProvide.extract(provide) },
        installIf = installIf.map { ins -> ApkIndexPackageInstallIf.extract(ins) },
        raw = entry
      )
    }
  }

  override fun toString(): String = "pkg(${id})"
}
