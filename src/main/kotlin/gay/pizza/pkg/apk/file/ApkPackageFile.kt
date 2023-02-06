package gay.pizza.pkg.apk.file

import gay.pizza.pkg.PlatformProcessSpawner
import gay.pizza.pkg.apk.core.ApkDataReader
import gay.pizza.pkg.apk.core.entries
import gay.pizza.pkg.fetch.ContentFetcher
import gay.pizza.pkg.fetch.FetchRequest
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.java.toJavaPath
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
      ApkInstalledFile(
        name = entry.name,
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

  fun metadataDataStream(): ArchiveInputStream {
    val stream = path.toJavaPath().toFile().inputStream().buffered()
    val reader = ApkDataReader(stream)
    reader.readCompressedStream().readAllBytes()
    return reader.readTarStream()
  }

  fun packageDataStream(): ArchiveInputStream {
    val stream = path.toJavaPath().toFile().inputStream().buffered()
    val reader = ApkDataReader(stream)
    reader.readCompressedStream().readAllBytes()
    reader.readCompressedStream().readAllBytes()
    return reader.readTarStream()
  }

  companion object {
    fun download(url: String, to: FsPath, fetcher: ContentFetcher): ApkPackageFile {
      fetcher.download(FetchRequest(url, userAgent = "apk-tools-kotlin/1.0"), to)
      return ApkPackageFile(to)
    }
  }
}
