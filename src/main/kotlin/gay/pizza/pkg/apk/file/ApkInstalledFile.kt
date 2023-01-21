package gay.pizza.pkg.apk.file

class ApkInstalledFile(
  val name: String,
  val size: Long,
  val isDirectory: Boolean
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    other as ApkInstalledFile
    if (name != other.name) return false
    if (size != other.size) return false
    if (isDirectory != other.isDirectory) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + size.hashCode()
    result = 31 * result + isDirectory.hashCode()
    return result
  }

  override fun toString(): String = "ApkInstalledFile(${name})"
}
