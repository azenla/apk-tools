package gay.pizza.pkg.apk.core

import gay.pizza.pkg.apk.index.ApkRawIndex

interface ApkInstalledDatabase {
  fun read(): ApkRawIndex
}
