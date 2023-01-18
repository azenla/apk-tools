package gay.pizza.pkg.apk

class ApkIndexPackageInstallIf(id: String) : ApkIndexRequirementRef(id) {
  companion object {
    fun extract(installIf: String): ApkIndexPackageInstallIf = ApkIndexPackageInstallIf(installIf)
  }
}
