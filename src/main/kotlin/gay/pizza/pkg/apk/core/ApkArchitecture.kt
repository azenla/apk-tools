package gay.pizza.pkg.apk.core

interface ApkArchitecture {
  fun read(): List<String>
  fun write(arches: List<String>)
}
