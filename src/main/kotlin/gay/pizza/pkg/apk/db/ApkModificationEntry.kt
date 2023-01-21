package gay.pizza.pkg.apk.db

class ApkModificationEntry(val directory: String) {
  var directoryAcl: String? = null
  var fileName: String? = null
  var fileAcl: String? = null
  var fileChecksum: String? = null

  override fun toString(): String =
    "mod(directory=$directory, directoryAcl=$directoryAcl, fileName=$fileName, fileAcl=$fileAcl, fileChecksum=$fileChecksum)"
}
