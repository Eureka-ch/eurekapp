// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_files")
data class DownloadedFile(
    @PrimaryKey val url: String,
    val localPath: String,
    val fileName: String
)