package gay.pizza.pkg.apk

class ApkIndexPackage(
  val repo: String,
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
  val dependencies: List<ApkIndexPackageDependency>,
  val provides: List<ApkIndexPackageProvide>,
  val installIf: List<ApkIndexPackageInstallIf>,
  val raw: ApkRawIndexEntry
) : ApkIndexRequirementRef(id) {
  val downloadUrl: String = "${repo}/${id}-${version}.apk"

  companion object {
    fun extract(repo: String, entry: ApkRawIndexEntry): ApkIndexPackage {
      fun ApkRawIndexEntry.required(key: String): String =
        data[key] ?: throw RuntimeException("entry key $key missing (${entry.data})")

      val checksum = entry.required("C")
      val id = entry.required("P")
      val version = entry.required("V")
      val architecture = entry.data["A"]
      val packageSize = entry.required("S").toLong()
      val installedSize = entry.required("I").toLong()
      val description = entry.required("T")
      val url = entry.required("U")
      val license = entry.required("L")
      val origin = entry.data["o"]
      val maintainer = entry.data["m"]
      val buildTime = entry.data["t"]?.toLong()
      val commit = entry.data["c"]
      val providerPriority = entry.data["k"]?.toInt()
      val dependencies = (entry.data["D"] ?: "").split(" ").filter { it.isNotBlank() }
      val provides = (entry.data["p"] ?: "").split(" ").filter { it.isNotBlank() }
      val installIf = (entry.data["i"] ?: "").split(" ").filter { it.isNotBlank() }

      return ApkIndexPackage(
        repo = repo,
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
        dependencies = dependencies.mapNotNull { dependency -> ApkIndexPackageDependency.extract(dependency) },
        provides = provides.mapNotNull { provide -> ApkIndexPackageProvide.extract(provide) },
        installIf = installIf.map { ins -> ApkIndexPackageInstallIf.extract(ins) },
        raw = entry
      )
    }
  }

  override fun toString(): String = "pkg(${id})"
}
