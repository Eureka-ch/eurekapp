// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class DownloadService(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
  private val client = OkHttpClient()

  suspend fun downloadFile(url: String, fileName: String): Result<Uri> =
      withContext(dispatcher) {
        try {
          val downloadsDir = File(context.cacheDir, "downloads")
          if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
          }

          val sanitizedFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
          val file = File(downloadsDir, sanitizedFileName)

          val request = Request.Builder().url(url).build()
          client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
              throw Exception("Download failed: ${response.code}")
            }
            response.body?.byteStream()?.use { input ->
              FileOutputStream(file).use { output -> input.copyTo(output) }
            } ?: throw Exception("Empty response body")
          }

          val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
          Result.success(uri)
        } catch (e: Exception) {
          Result.failure(e)
        }
      }
}
