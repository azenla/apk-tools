package gay.pizza.pkg.apk.file

import gay.pizza.pkg.PlatformProcessSpawner
import gay.pizza.pkg.apk.core.ApkDataReader
import gay.pizza.pkg.apk.index.ApkPackageNotFoundException
import gay.pizza.pkg.apk.core.entries
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.delete
import gay.pizza.pkg.io.java.toFsPath
import gay.pizza.pkg.io.java.toJavaPath
import gay.pizza.pkg.process.command.CommandName
import gay.pizza.pkg.process.command.RawArgument
import gay.pizza.pkg.process.command.RelativeDirectoryPath
import org.apache.commons.compress.archivers.ArchiveInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
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
    fun download(url: String, to: FsPath, client: HttpClient? = null): ApkPackageFile {
      val clientForDownload = client ?: HttpClient.newHttpClient()

      val request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .header("User-Agent", "apk-tools-kotlin/1.0")
        .build()

      val response = clientForDownload.send(request, BodyHandlers.ofFile(to.toJavaPath()))
      if (response.statusCode() != 200) {
        to.delete()
        throw ApkPackageNotFoundException(url)
      }
      return ApkPackageFile(response.body().toFsPath())
    }
  }
}
