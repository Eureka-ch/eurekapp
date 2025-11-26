// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class DownloadedFileDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: DownloadedFileDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        dao = database.downloadedFileDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun downloadedFileDao_getAll() = runBlocking {
        // Given
        val file1 = DownloadedFile("http://test.com/file1.jpg", "/path/file1.jpg", "file1.jpg")
        val file2 = DownloadedFile("http://test.com/file2.pdf", "/path/file2.pdf", "file2.pdf")

        // When
        dao.insert(file1)
        dao.insert(file2)
        val result = dao.getAll().first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.any { it.url == file1.url })
        assertTrue(result.any { it.url == file2.url })
    }

    @Test
    fun downloadedFileDao_delete() = runBlocking {
        // Given
        val file = DownloadedFile("http://test.com/file.jpg", "/path/file.jpg", "file.jpg")
        dao.insert(file)

        // When
        dao.delete(file)
        val result = dao.getAll().first()

        // Then
        assertFalse(result.any { it.url == file.url })
    }
}
