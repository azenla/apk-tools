package gay.pizza.pkg.fetch

import gay.pizza.pkg.io.FsPath
import java.io.InputStream

interface ContentFetcher {
  fun download(request: FetchRequest, filePath: FsPath)
  fun read(request: FetchRequest): InputStream
}
