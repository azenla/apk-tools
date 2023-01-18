package gay.pizza.pkg.io

interface FsPathVisitor {
  fun beforeVisitDirectory(path: FsPath): VisitResult
  fun visitFile(path: FsPath): VisitResult
  fun visitFileFailed(path: FsPath, exception: Exception): VisitResult
  fun afterVisitDirectory(path: FsPath): VisitResult

  enum class VisitResult {
    Continue,
    Terminate,
    SkipSubtree,
    SkipSiblings
  }
}
