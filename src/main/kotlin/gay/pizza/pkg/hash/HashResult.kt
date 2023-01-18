package gay.pizza.pkg.hash

interface HashResult {
  val bytes: ByteArray
  fun toHexString(): String
}
