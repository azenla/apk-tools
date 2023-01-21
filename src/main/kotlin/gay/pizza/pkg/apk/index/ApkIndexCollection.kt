package gay.pizza.pkg.apk.index

interface ApkIndexCollection {
  val indexes: List<ApkIndex>
  val index: ApkIndex

  fun clean()
  fun download(url: String)
}
