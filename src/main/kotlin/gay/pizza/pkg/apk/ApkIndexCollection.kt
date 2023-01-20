package gay.pizza.pkg.apk

interface ApkIndexCollection {
  val indexes: List<ApkIndex>
  val index: ApkIndex

  fun clean()
  fun download(url: String)
}
