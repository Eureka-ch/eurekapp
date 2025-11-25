// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_files")
data class DownloadedFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val fileName: String,
    val localPath: String,
    val downloadDate: Long = System.currentTimeMillis()
)