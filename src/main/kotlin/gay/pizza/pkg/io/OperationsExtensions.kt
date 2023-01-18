package gay.pizza.pkg.io

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import java.time.Instant

fun FsPath.exists(): Boolean =
  operations.exists(this)

fun FsPath.isDirectory(): Boolean =
  operations.isDirectory(this)

fun FsPath.isRegularFile(): Boolean =
  operations.isRegularFile(this)

fun FsPath.isSymbolicLink(): Boolean =
  operations.isSymbolicLink(this)

fun FsPath.isReadable(): Boolean =
  operations.isReadable(this)

fun FsPath.isWritable(): Boolean =
  operations.isWritable(this)

fun FsPath.isExecutable(): Boolean =
  operations.isExecutable(this)

fun FsPath.list(): Sequence<FsPath> =
  operations.list(this)

fun FsPath.walk(): Sequence<FsPath> =
  operations.walk(this)

fun FsPath.visit(visitor: FsPathVisitor): Unit =
  operations.visit(this, visitor)

fun FsPath.readString(): String =
  operations.readString(this)

fun FsPath.readAllBytes(): ByteArray =
  operations.readAllBytes(this)

fun FsPath.readBytesChunked(block: (ByteArray, Int) -> Unit) =
  operations.readBytesChunked(this, block)

fun <T> FsPath.readJsonFile(deserializer: DeserializationStrategy<T>): T =
  operations.readJsonFile(this, deserializer)

fun FsPath.readLines(block: (String) -> Unit) =
  operations.readLines(this, block)

fun <T> FsPath.readJsonLines(deserializer: DeserializationStrategy<T>, block: (T) -> Unit) =
  operations.readJsonLines(this, deserializer, block)

fun <T> FsPath.readJsonLines(deserializer: DeserializationStrategy<T>): List<T> =
  operations.readJsonLines(this, deserializer)

fun FsPath.writeString(content: String): Unit =
  operations.writeString(this, content)

fun FsPath.writeAllBytes(bytes: ByteArray): Unit =
  operations.writeAllBytes(this, bytes)

fun <T> FsPath.writeJsonFile(serializer: SerializationStrategy<T>, value: T): Unit =
  operations.writeJsonFile(this, serializer, value)

fun FsPath.delete(): Unit =
  operations.delete(this)

fun FsPath.deleteOnExit(): Unit =
  operations.delete(this)

fun FsPath.deleteRecursively(): Unit =
  operations.deleteRecursively(this)

fun FsPath.lastModifiedTime(): Instant =
  operations.lastModifiedTime(this)

fun FsPath.createDirectories(): Unit =
  operations.createDirectories(this)
