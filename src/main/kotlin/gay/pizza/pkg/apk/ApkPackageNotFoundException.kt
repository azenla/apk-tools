package gay.pizza.pkg.apk

class ApkPackageNotFoundException(val url: String) : RuntimeException("Package at URL ${url} not found.")
