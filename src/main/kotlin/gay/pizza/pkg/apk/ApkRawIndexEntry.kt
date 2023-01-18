package gay.pizza.pkg.apk

class ApkRawIndexEntry(
  val data: Map<String, String>
) {
  companion object {
    fun parseEntryContent(lines: Sequence<String>): ApkRawIndexEntry = ApkRawIndexEntry(
      lines.map { line ->
        line.split(":", limit = 2)
      }.map { line ->
        line[0] to line[1]
      }.toMap()
    )
  }
}
