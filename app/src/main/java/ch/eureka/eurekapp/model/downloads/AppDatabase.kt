// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownloadedFile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun downloadedFileDao(): DownloadedFileDao

  companion object {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE
          ?: synchronized(this) {
            val instance =
                Room.databaseBuilder(
                        context.applicationContext, AppDatabase::class.java, "eurekapp_database")
                    .fallbackToDestructiveMigration(true)
                    .build()
            INSTANCE = instance
            instance
          }
    }
  }
}
