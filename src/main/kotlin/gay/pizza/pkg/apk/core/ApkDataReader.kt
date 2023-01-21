package gay.pizza.pkg.apk.core

import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.Closeable
import java.io.InputStream

class ApkDataReader(val stream: InputStream) : Closeable {
  fun readCompressedStream(): CompressorInputStream {
    return compressorStreamFactory.createCompressorInputStream(CompressorStreamFactory.GZIP, stream)
  }

  fun readTarStream(): ArchiveInputStream {
    val stream = readCompressedStream()
    return archiveStreamFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, stream)
  }

  companion object {
    private val compressorStreamFactory = CompressorStreamFactory()
    private val archiveStreamFactory = ArchiveStreamFactory()
  }

  override fun close() {
    stream.close()
  }
}
