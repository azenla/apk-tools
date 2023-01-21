package gay.pizza.pkg.apk.index

import gay.pizza.pkg.apk.db.ApkModificationEntry

class ApkRawIndexEntry(
  val data: List<Pair<String, String>>
) {
  companion object {
    fun parseEntryContent(lines: Sequence<String>): ApkRawIndexEntry = ApkRawIndexEntry(
      lines.map { line ->
        line.split(":", limit = 2)
      }.map { line ->
        line[0] to line[1]
      }.toList()
    )
  }

  fun toMap(): Map<String, String> = data.toMap()

  fun modifications(): List<ApkModificationEntry> {
    val items = data.dropWhile { it.first != "F" }
    val entries = mutableListOf<ApkModificationEntry>()
    var entry: ApkModificationEntry? = null
    for (item in items) {
      when (item.first) {
        "F" -> {
          if (entry != null) {
            entries.add(entry)
          }
          entry = ApkModificationEntry(item.second)
        }
        "M" -> {
          entry?.directoryAcl = item.second
        }
        "R" -> {
          entry?.fileName = item.second
        }
        "a" -> {
          entry?.fileAcl = item.second
        }
        "Z" -> {
          entry?.fileChecksum = item.second
        }
      }
    }
    return entries
  }

  fun lookup(key: String): String? = data.firstOrNull { it.first == key }?.second

  override fun toString(): String = data.toString()
}
