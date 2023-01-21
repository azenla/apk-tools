package gay.pizza.pkg.apk.index

class ApkPackageNotFoundException(val url: String) : RuntimeException("Package at URL ${url} not found.")
