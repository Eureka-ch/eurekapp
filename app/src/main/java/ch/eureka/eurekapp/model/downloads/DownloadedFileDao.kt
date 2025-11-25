// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedFileDao {
    @Insert
    suspend fun insert(downloadedFile: DownloadedFile)

    @Query("SELECT * FROM downloaded_files WHERE url = :url")
    suspend fun getByUrl(url: String): DownloadedFile?

    @Query("SELECT * FROM downloaded_files")
    fun getAll(): Flow<List<DownloadedFile>>
}
