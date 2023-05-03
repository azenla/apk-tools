package gay.pizza.pkg.apk.file

import java.util.TreeMap

class ApkFsState {
  val entities: TreeMap<String, ApkFsEntity> = TreeMap { a, b ->
    val compare = a.count { it == '/' }.compareTo(b.count { it == '/' })
    return@TreeMap if (compare == 0) {
      a.compareTo(b)
    } else {
      compare
    }
  }

  fun addPackageFile(file: ApkPackageFile) {
    for (item in file.listInstalledFiles()) {
      entities[item.name] = ApkFsEntity(
        name = item.name.split("/").last(),
        path = item.name,
        type = if (item.isDirectory) ApkFsEntityType.Directory else ApkFsEntityType.File
      )
    }
  }

  fun putAllEntities(map: Map<String, ApkFsEntity>) {
    entities.putAll(map)
  }

  fun commonalities(other: ApkFsState): ApkFsState {
    val shared = entities.keys.intersect(other.entities.keys)
    val result = ApkFsState()
    result.putAllEntities(shared.associateWith { entities[it]!! })
    return result
  }
}
