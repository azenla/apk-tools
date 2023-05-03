package gay.pizza.pkg.apk.file

import gay.pizza.pkg.PlatformProcessSpawner
import gay.pizza.pkg.apk.core.ApkDataReader
import gay.pizza.pkg.apk.core.entries
import gay.pizza.pkg.apk.index.ApkPackageNotFoundException
import gay.pizza.pkg.apk.io.inputStream
import gay.pizza.pkg.fetch.ContentFetcher
import gay.pizza.pkg.fetch.ContentNotFoundException
import gay.pizza.pkg.fetch.FetchRequest
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.process.command.CommandName
import gay.pizza.pkg.process.command.RawArgument
import gay.pizza.pkg.process.command.RelativeDirectoryPath
import org.apache.commons.compress.archivers.ArchiveInputStream
import java.nio.charset.StandardCharsets

class ApkPackageFile(val path: FsPath) {
  fun extract(to: FsPath) {
    val result = PlatformProcessSpawner.execute(listOf(
      CommandName("tar"),
      RawArgument("zxf"),
      RelativeDirectoryPath(path)
    ), workingDirectoryPath = to)

    if (result.exitCode != 0) {
      throw RuntimeException("Failed to extract ${path.fullPathString} to ${to.fullPathString}")
    }
  }

  fun listInstalledFiles(): List<ApkInstalledFile> = packageDataStream().use { stream ->
    stream.entries.map { entry ->
      var path = entry.name
      if (path.endsWith("/")) {
        path = path.substring(0, path.length - 1)
      }
      ApkInstalledFile(
        name = path,
        size = entry.size,
        isDirectory = entry.isDirectory
      )
    }.toList()
  }

  fun packageInfo(): ApkPkgInfo = metadataDataStream().use { stream ->
    stream.entries.first { it.name == ".PKGINFO" }
    val lines = stream.readAllBytes().toString(StandardCharsets.UTF_8).lineSequence()
    return ApkPkgInfo.parse(lines)
  }

  fun tarDataStream(index: Int): ArchiveInputStream {
    val stream = path.inputStream().buffered()
    val reader = ApkDataReader(stream)
    var current = 0
    while (current < index) {
      reader.readCompressedStream().readAllBytes()
      current++
    }
    return reader.readTarStream()
  }

  fun signatureDataStream(): ArchiveInputStream = tarDataStream(0)
  fun metadataDataStream(): ArchiveInputStream = tarDataStream(1)
  fun packageDataStream(): ArchiveInputStream = tarDataStream(2)

  companion object {
    fun download(url: String, to: FsPath, fetcher: ContentFetcher): ApkPackageFile {
      try {
        fetcher.download(FetchRequest(url, userAgent = "apk-tools-kotlin/1.0"), to)
      } catch (e: ContentNotFoundException) {
        throw ApkPackageNotFoundException(e.request.url)
      }
      return ApkPackageFile(to)
    }
  }
}
