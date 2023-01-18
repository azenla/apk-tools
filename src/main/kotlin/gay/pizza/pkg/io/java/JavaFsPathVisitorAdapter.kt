package gay.pizza.pkg.io.java

import gay.pizza.pkg.io.FsPathVisitor
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class JavaFsPathVisitorAdapter(private val visitor: FsPathVisitor) : FileVisitor<Path> {
  override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
    return visitor.beforeVisitDirectory(dir!!.toFsPath()).adapt()
  }

  override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
    return visitor.visitFile(file!!.toFsPath()).adapt()
  }

  override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
    return visitor.visitFileFailed(file!!.toFsPath(), exc!!).adapt()
  }

  override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
    return visitor.afterVisitDirectory(dir!!.toFsPath()).adapt()
  }

  private fun FsPathVisitor.VisitResult.adapt(): FileVisitResult = when (this) {
    FsPathVisitor.VisitResult.Continue -> FileVisitResult.CONTINUE
    FsPathVisitor.VisitResult.Terminate -> FileVisitResult.TERMINATE
    FsPathVisitor.VisitResult.SkipSubtree -> FileVisitResult.SKIP_SUBTREE
    FsPathVisitor.VisitResult.SkipSiblings -> FileVisitResult.SKIP_SIBLINGS
  }
}
