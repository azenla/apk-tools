package gay.pizza.pkg.fetch

class ContentNotFoundException(val request: FetchRequest) : RuntimeException("Content Not Found")
