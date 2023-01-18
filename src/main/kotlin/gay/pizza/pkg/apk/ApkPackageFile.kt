package gay.pizza.pkg.apk

import gay.pizza.pkg.PlatformProcessSpawner
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.java.toJavaPath
import gay.pizza.pkg.process.command.CommandName
import gay.pizza.pkg.process.command.RawArgument
import gay.pizza.pkg.process.command.RelativeDirectoryPath
import org.apache.commons.compress.archivers.ArchiveInputStream
import java.io.FileOutputStream
import java.net.URL

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

  fun listInstalledFiles(): List<ApkInstalledFile> = packageDataStream().entries.map { entry ->
    ApkInstalledFile(
      name = entry.name,
      size = entry.size,
      isDirectory = entry.isDirectory
    )
  }.toList()

  fun packageDataStream(): ArchiveInputStream {
    val stream = path.toJavaPath().toFile().inputStream().buffered()
    val reader = ApkDataReader(stream)
    reader.readCompressedStream().readAllBytes()
    reader.readCompressedStream().readAllBytes()
    return reader.readTarStream()
  }

  companion object {
    fun download(url: String, to: FsPath): ApkPackageFile {
      val javaUrl = URL(url)
      val stream = javaUrl.openStream()
      val out = FileOutputStream(to.toJavaPath().toFile())
      stream.copyTo(out)
      stream.close()
      out.close()
      return ApkPackageFile(to)
    }
  }
}
