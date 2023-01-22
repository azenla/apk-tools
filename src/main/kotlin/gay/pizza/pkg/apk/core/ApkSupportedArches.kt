package gay.pizza.pkg.apk.core

interface ApkSupportedArches {
  fun read(): List<String>
  fun write(arches: List<String>)
}
