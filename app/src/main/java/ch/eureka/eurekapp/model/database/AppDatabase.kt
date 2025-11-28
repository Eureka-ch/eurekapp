/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.eureka.eurekapp.model.database.entities.MessageEntity

/**
 * The Room Database holder for the application.
 *
 * This abstract class serves as the main access point for the underlying SQLite connection. It
 * defines the entities (tables) and version of the database.
 */
@Database(entities = [MessageEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

  /** Provides access to the [MessageDao] for interacting with the `local_notes` table. */
  abstract fun messageDao(): MessageDao
}
