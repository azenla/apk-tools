package gay.pizza.pkg.apk.file

import gay.pizza.pkg.apk.index.ApkRawIndexEntry

class ApkPkgInfo(val items: List<Pair<String, String>>) {
  companion object {
    private val keyToRaw = mapOf(
      "pkgname" to "P",
      "pkgver" to "V",
      "pkgdesc" to "T",
      "url" to "U",
      "builddate" to "t",
      "size" to "S",
      "arch" to "A",
      "origin" to "o",
      "commit" to "c",
      "maintainer" to "m",
      "license" to "L",
      "depend" to "D",
      "provides" to "p",
      "provider_priority" to "k",
      "install_if" to "i"
    )

    fun parse(lines: Sequence<String>): ApkPkgInfo = ApkPkgInfo(lines
      .filter { line -> !line.startsWith("#") }
      .map { line -> line.split(" = ", limit = 2) }
      .filter { it.size == 2 }
      .map { parts -> parts[0] to parts[1] }.toList()
    )
  }

  fun shrink(): ApkPkgInfo {
    val data = mutableMapOf<String, String>()
    for (item in items) {
      if (data.containsKey(item.first)) {
        data[item.first] += " ${item.second}"
      } else {
        data[item.first] = item.second
      }
    }
    return ApkPkgInfo(data.map { it.key to it.value })
  }

  fun toRawIndexEntry(): ApkRawIndexEntry {
    val result = mutableMapOf<String, String>()
    for ((key, raw) in keyToRaw) {
      val value = items.singleOrNull { it.first == key }?.second ?: continue
      result[raw] = value
    }
    return ApkRawIndexEntry(result.map { it.key to it.value })
  }

  override fun toString(): String = "PKGINFO(${items})"
}
