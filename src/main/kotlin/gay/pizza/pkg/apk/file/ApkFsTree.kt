package gay.pizza.pkg.apk.file

class ApkFsTree {
  val root: ApkFsTreeEntry = ApkFsTreeEntry(
    entity = ApkFsEntity(
      name = "",
      path = "/",
      type = ApkFsEntityType.Directory
    )
  )

  fun populate(state: ApkFsState) {
    for ((path, entity) in state.entities) {
      add(path, entity)
    }
  }

  fun add(path: String, entity: ApkFsEntity): ApkFsTreeEntry {
    val components = path.splitToSequence("/").toMutableList()
    if (components.first().isEmpty()) {
      components.removeFirst()
    }
    components.removeLast()
    var previous = root
    var previousPath = ""
    for (component in components) {
      val currentPath = "$previousPath/$component"
      previous = previous.getOrCreateStub(component, currentPath)
      previousPath = currentPath
    }
    return previous.addChild(entity)
  }
}
