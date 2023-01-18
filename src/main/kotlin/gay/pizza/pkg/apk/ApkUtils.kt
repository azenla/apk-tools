package gay.pizza.pkg.apk

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream

val ArchiveInputStream.entries: Sequence<ArchiveEntry>
  get() = generateSequence { nextEntry }
