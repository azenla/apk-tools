package gay.pizza.pkg.io

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import gay.pizza.pkg.PlatformPath
import kotlinx.serialization.DeserializationStrategy

private fun pathType(context: Context, fileOkay: Boolean, folderOkay: Boolean): String = when {
  fileOkay && !folderOkay -> context.localization.pathTypeFile()
  !fileOkay && folderOkay -> context.localization.pathTypeDirectory()
  else -> context.localization.pathTypeOther()
}

internal fun convertToPath(
  path: String,
  mustExist: Boolean,
  canBeFile: Boolean,
  canBeFolder: Boolean,
  mustBeWritable: Boolean,
  mustBeReadable: Boolean,
  canBeSymlink: Boolean,
  context: Context,
  fail: (String) -> Unit,
): FsPath {
  val name = pathType(context, canBeFile, canBeFolder)
  return with(context.localization) {
    PlatformPath(path).also {
      if (mustExist && !it.exists()) fail(pathDoesNotExist(name, it.toString()))
      if (!canBeFile && it.isRegularFile()) fail(pathIsFile(name, it.toString()))
      if (!canBeFolder && it.isDirectory()) fail(pathIsDirectory(name, it.toString()))
      if (mustBeWritable && !it.isWritable()) fail(pathIsNotWritable(name, it.toString()))
      if (mustBeReadable && !it.isReadable()) fail(pathIsNotReadable(name, it.toString()))
      if (!canBeSymlink && it.isSymbolicLink()) fail(pathIsSymlink(name, it.toString()))
    }
  }
}

fun RawArgument.fsPath(
  mustExist: Boolean = false,
  canBeFile: Boolean = true,
  canBeDir: Boolean = true,
  mustBeWritable: Boolean = false,
  mustBeReadable: Boolean = false,
  canBeSymlink: Boolean = true
): ProcessedArgument<FsPath, FsPath> {
  return convert(completionCandidates = CompletionCandidates.Path) { str ->
    convertToPath(
      str,
      mustExist,
      canBeFile,
      canBeDir,
      mustBeWritable,
      mustBeReadable,
      canBeSymlink,
      context
    ) { fail(it) }
  }
}

fun RawOption.fsPath(
  mustExist: Boolean = false,
  canBeFile: Boolean = true,
  canBeDir: Boolean = true,
  mustBeWritable: Boolean = false,
  mustBeReadable: Boolean = false,
  canBeSymlink: Boolean = true
): NullableOption<FsPath, FsPath> {
  return convert({ localization.pathMetavar() }, CompletionCandidates.Path) { str ->
    convertToPath(
      str,
      mustExist,
      canBeFile,
      canBeDir,
      mustBeWritable,
      mustBeReadable,
      canBeSymlink,
      context
    ) { fail(it) }
  }
}


fun <T : Any> RawArgument.fsJsonFile(deserializer: DeserializationStrategy<T>): ProcessedArgument<T, T> {
  return convert(completionCandidates = CompletionCandidates.Path) { str ->
    val fsPath = convertToPath(
      str,
      mustExist = true,
      canBeFile = true,
      canBeFolder = false,
      canBeSymlink = true,
      mustBeReadable = true,
      mustBeWritable = false,
      context = context
    ) { fail(it) }
    fsPath.readJsonFile(deserializer)
  }
}

fun <T : Any> RawOption.fsJsonFile(deserializer: DeserializationStrategy<T>): NullableOption<T, T> {
  return convert(completionCandidates = CompletionCandidates.Path) { str ->
    val fsPath = convertToPath(
      str,
      mustExist = true,
      canBeFile = true,
      canBeFolder = false,
      canBeSymlink = true,
      mustBeReadable = true,
      mustBeWritable = false,
      context = context
    ) { fail(it) }
    fsPath.readJsonFile(deserializer)
  }
}
