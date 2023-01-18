package gay.pizza.pkg.apk

import java.io.InputStream
import java.nio.charset.StandardCharsets

class ApkRawIndex(
  val packages: List<ApkRawIndexEntry>
) {
  companion object {
    fun parseIndexContent(lines: Sequence<String>): ApkRawIndex {
      val stack = mutableListOf<String>()

      val packages = mutableListOf<ApkRawIndexEntry>()
      fun pop() {
        if (stack.isEmpty()) {
          return
        }

        val entrySequence = stack.asSequence()
        val entry = ApkRawIndexEntry.parseEntryContent(entrySequence)
        packages.add(entry)
        stack.clear()
      }

      for (line in lines) {
        if (line.isBlank()) {
          pop()
          continue
        }
        stack.add(line)
      }
      pop()
      return ApkRawIndex(packages)
    }

    fun parseGzipTarIndex(stream: InputStream): ApkRawIndex {
      val reader = ApkDataReader(stream)
      val signatureTarStream = reader.readTarStream()
      generateSequence { signatureTarStream.nextEntry }.forEach { _ ->
        signatureTarStream.readAllBytes()
      }
      val indexTarStream = reader.readTarStream()
      indexTarStream.entries.first { it.name == "APKINDEX" }
      val indexFileBytes = indexTarStream.readAllBytes()
      val indexFileString = indexFileBytes.toString(StandardCharsets.UTF_8)
      stream.close()
      return parseIndexContent(indexFileString.lineSequence())
    }
  }
}
