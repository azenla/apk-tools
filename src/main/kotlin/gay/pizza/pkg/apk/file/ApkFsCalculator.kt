package gay.pizza.pkg.apk.file

class ApkFsCalculator {
  val entries = mutableMapOf<String, ApkFsEntry>()

  fun addPackageFile(file: ApkPackageFile) {
    for (item in file.listInstalledFiles()) {
      if (entries.containsKey(item.name)) {
        conflict(item.name)
      }
      entries[item.name] = ApkFsEntry(
        type = if (item.isDirectory) ApkFsEntryType.Directory else ApkFsEntryType.File
      )
    }
  }

  fun conflict(path: String) {
    //println("CONFLICT $path")
  }
}
