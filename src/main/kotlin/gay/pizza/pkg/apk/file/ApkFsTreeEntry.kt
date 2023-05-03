package gay.pizza.pkg.apk.file

class ApkFsTreeEntry(
  var parent: ApkFsTreeEntry? = null,
  val entity: ApkFsEntity,
  val children: MutableMap<String, ApkFsTreeEntry> = mutableMapOf()
) {
  fun addChild(entity: ApkFsEntity): ApkFsTreeEntry {
    val entry = ApkFsTreeEntry(
      parent = this,
      entity = entity
    )
    children[entity.name] = entry
    return entry
  }

  fun getOrCreateStub(name: String, path: String): ApkFsTreeEntry {
    return children[name] ?: addChild(ApkFsEntity(name, path, ApkFsEntityType.Directory))
  }

  fun crawl(block: (ApkFsTreeEntry) -> Unit) {
    block(this)
    for (child in children.values) {
      child.crawl(block)
    }
  }
}
