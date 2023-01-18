@file:Suppress("FunctionName")
package gay.pizza.pkg

import gay.pizza.pkg.concurrent.TaskPool
import gay.pizza.pkg.concurrent.java.JavaTaskPool
import gay.pizza.pkg.hash.Hash
import gay.pizza.pkg.hash.java.JavaHash
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.java.JavaPath
import gay.pizza.pkg.process.ProcessExecutor
import gay.pizza.pkg.process.java.JavaProcessExecutor
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.absolute

fun PlatformInit() {}
fun PlatformPath(path: String): FsPath = JavaPath(Path.of(path))
fun PlatformEpochMilliseconds(): Long = System.currentTimeMillis()
fun PlatformPathSeparator(): Char = File.pathSeparatorChar
fun PlatformCurrentWorkingDirectory(): FsPath = JavaPath(Path.of(".").absolute())
fun PlatformCreateTempDirectory(): FsPath = JavaPath(Files.createTempDirectory("syscan"))
fun <K, V> PlatformConcurrentMap(): MutableMap<K, V> = ConcurrentHashMap()
fun PlatformTaskPool(concurrency: Int): TaskPool = JavaTaskPool(concurrency)
fun PlatformHash(name: String): Hash = JavaHash(MessageDigest.getInstance(name), name)
val PlatformProcessSpawner: ProcessExecutor = JavaProcessExecutor
