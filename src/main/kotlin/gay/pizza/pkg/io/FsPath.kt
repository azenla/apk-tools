package gay.pizza.pkg.io

import kotlinx.serialization.Serializable

@Serializable(with = FsPathSerializer::class)
interface FsPath : Comparable<FsPath> {
  val fullPathString: String
  val entityNameString: String

  val parent: FsPath?
  val operations: FsOperations

  fun resolve(part: String): FsPath
  fun relativeTo(path: FsPath): FsPath

  override fun compareTo(other: FsPath): Int {
    return fullPathString.compareTo(other.fullPathString)
  }
}
