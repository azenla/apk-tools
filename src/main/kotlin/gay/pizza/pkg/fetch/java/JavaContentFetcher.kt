package gay.pizza.pkg.fetch.java

import gay.pizza.pkg.fetch.ContentFetcher
import gay.pizza.pkg.fetch.ContentNotFoundException
import gay.pizza.pkg.fetch.FetchRequest
import gay.pizza.pkg.io.FsPath
import gay.pizza.pkg.io.delete
import gay.pizza.pkg.io.java.toJavaPath
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.BodyHandlers

class JavaContentFetcher(val httpClient: HttpClient = HttpClient.newHttpClient()) : ContentFetcher {
  override fun download(request: FetchRequest, filePath: FsPath) {
    handleGetRequest(request, BodyHandlers.ofFile(filePath.toJavaPath()), fail = {
      filePath.delete()
    })
  }

  override fun read(request: FetchRequest): InputStream {
    return handleGetRequest(request, BodyHandlers.ofInputStream())
  }

  private fun <T> handleGetRequest(fetch: FetchRequest, bodyHandler: BodyHandler<T>, fail: () -> Unit = {}): T {
    val request = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create(fetch.url))
      .header("User-Agent", fetch.userAgent)
      .build()
    val response = httpClient.send(request, bodyHandler)
    if (response.statusCode() != 200) {
      fail()

      if (response.statusCode() == 404) {
        throw ContentNotFoundException(fetch)
      }
      throw RuntimeException("Fetch of ${fetch.url} failed (Status Code ${response.statusCode()})")
    }
    return response.body()
  }
}
