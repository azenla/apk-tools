package gay.pizza.pkg.apk.core

interface ApkWorld {
  fun read(): List<String>
  fun write(packages: List<String>)
}
