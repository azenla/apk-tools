package gay.pizza.pkg.io

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import java.time.Instant

interface FsOperations {
  fun exists(path: FsPath): Boolean
  fun isDirectory(path: FsPath): Boolean
  fun isRegularFile(path: FsPath): Boolean
  fun isSymbolicLink(path: FsPath): Boolean
  fun isReadable(path: FsPath): Boolean
  fun isWritable(path: FsPath): Boolean
  fun isExecutable(path: FsPath): Boolean

  fun list(path: FsPath): Sequence<FsPath>

  fun walk(path: FsPath): Sequence<FsPath>
  fun visit(path: FsPath, visitor: FsPathVisitor)

  fun readString(path: FsPath): String
  fun readAllBytes(path: FsPath): ByteArray
  fun readBytesChunked(path: FsPath, block: (ByteArray, Int) -> Unit)
  fun <T> readJsonFile(path: FsPath, deserializer: DeserializationStrategy<T>): T

  fun readLines(path: FsPath, block: (String) -> Unit)
  fun <T> readJsonLines(path: FsPath, deserializer: DeserializationStrategy<T>, block: (T) -> Unit)
  fun <T> readJsonLines(path: FsPath, deserializer: DeserializationStrategy<T>): List<T>

  fun writeString(path: FsPath, content: String)
  fun writeAllBytes(path: FsPath, bytes: ByteArray)
  fun <T> writeJsonFile(path: FsPath, serializer: SerializationStrategy<T>, value: T)

  fun delete(path: FsPath)
  fun deleteOnExit(path: FsPath)
  fun deleteRecursively(path: FsPath)

  fun lastModifiedTime(path: FsPath): Instant

  fun createDirectories(path: FsPath)
}
