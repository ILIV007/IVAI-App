package dev.iliv007.ivai.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseMigrationRecoveryTest {

    @Test
    fun `legacy version one database upgrades through all migrations and recovers workspace data`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "migration-v1-to-v6-${System.nanoTime()}.db"
        context.deleteDatabase(databaseName)
        createVersionOneDatabase(context, databaseName)

        val upgraded = Room.databaseBuilder(context, IvaiDatabase::class.java, databaseName)
            .addMigrations(
                IvaiDatabase.MIGRATION_1_2,
                IvaiDatabase.MIGRATION_2_3,
                IvaiDatabase.MIGRATION_3_4,
                IvaiDatabase.MIGRATION_4_5,
                IvaiDatabase.MIGRATION_5_6
            )
            .allowMainThreadQueries()
            .build()
        try {
            assertEquals(6, upgraded.openHelper.writableDatabase.version)
            assertEquals("Legacy project", upgraded.projectDao().findById("legacy-project")?.name)
            assertEquals("legacy-project", upgraded.threadDao().findById("legacy-thread")?.projectId)
            val legacyMessage = upgraded.messageDao().listForThread("legacy-thread").single()
            assertEquals("پیام محلی قدیمی", legacyMessage.text)
            assertTrue(!legacyMessage.isIncomplete)

            // Opening the database validates Room's complete v6 schema. Querying the new DAO also
            // proves that Agent tables and provider trust/auth columns are available after upgrade.
            assertTrue(upgraded.agentProfileDao().observeAll().first().isEmpty())
        } finally {
            upgraded.close()
        }

        val reopened = Room.databaseBuilder(context, IvaiDatabase::class.java, databaseName)
            .addMigrations(
                IvaiDatabase.MIGRATION_1_2,
                IvaiDatabase.MIGRATION_2_3,
                IvaiDatabase.MIGRATION_3_4,
                IvaiDatabase.MIGRATION_4_5,
                IvaiDatabase.MIGRATION_5_6
            )
            .allowMainThreadQueries()
            .build()
        try {
            assertNotNull(reopened.projectDao().findById("legacy-project"))
            val reopenedMessage = reopened.messageDao().listForThread("legacy-thread").single()
            assertEquals("legacy-thread", reopenedMessage.threadId)
            assertTrue(!reopenedMessage.isIncomplete)
            assertTrue(reopened.agentProfileDao().observeAll().first().isEmpty())
        } finally {
            reopened.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun createVersionOneDatabase(context: Context, databaseName: String) {
        val databaseFile = context.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()
        val database = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        try {
            database.execSQL(
                "CREATE TABLE `workspace_projects` (" +
                    "`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, " +
                    "`fileCount` INTEGER NOT NULL, `updated_at_epoch_ms` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))"
            )
            database.execSQL(
                "CREATE TABLE `chat_threads` (" +
                    "`id` TEXT NOT NULL, `title` TEXT NOT NULL, `snippet` TEXT NOT NULL, " +
                    "`updated_at_epoch_ms` INTEGER NOT NULL, `modelOrCombo` TEXT NOT NULL, " +
                    "`project_id` TEXT, PRIMARY KEY(`id`), " +
                    "FOREIGN KEY(`project_id`) REFERENCES `workspace_projects`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE SET NULL)"
            )
            database.execSQL(
                "CREATE TABLE `chat_messages` (" +
                    "`id` TEXT NOT NULL, `thread_id` TEXT NOT NULL, `sender` TEXT NOT NULL, " +
                    "`text` TEXT NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, " +
                    "`content_type` TEXT NOT NULL, `code_snippet` TEXT, `model_badge` TEXT, " +
                    "`latency_ms` INTEGER, PRIMARY KEY(`id`), " +
                    "FOREIGN KEY(`thread_id`) REFERENCES `chat_threads`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
            )
            database.execSQL(
                "CREATE INDEX `index_workspace_projects_updated_at_epoch_ms` " +
                    "ON `workspace_projects` (`updated_at_epoch_ms`)"
            )
            database.execSQL(
                "CREATE INDEX `index_chat_threads_project_id` ON `chat_threads` (`project_id`)"
            )
            database.execSQL(
                "CREATE INDEX `index_chat_threads_updated_at_epoch_ms` " +
                    "ON `chat_threads` (`updated_at_epoch_ms`)"
            )
            database.execSQL(
                "CREATE INDEX `index_chat_messages_thread_id_created_at_epoch_ms` " +
                    "ON `chat_messages` (`thread_id`, `created_at_epoch_ms`)"
            )
            database.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            database.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, ?)",
                arrayOf("261e26c38b6ff9927fe268ff882fd393")
            )
            database.execSQL("PRAGMA user_version = 1")

            database.execSQL(
                "INSERT INTO workspace_projects VALUES(?, ?, ?, ?, ?)",
                arrayOf<Any?>("legacy-project", "Legacy project", "Created before provider registry", 0, 100L)
            )
            database.execSQL(
                "INSERT INTO chat_threads VALUES(?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>("legacy-thread", "Legacy chat", "پیام محلی قدیمی", 200L, "Unset", "legacy-project")
            )
            database.execSQL(
                "INSERT INTO chat_messages VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>("legacy-message", "legacy-thread", "USER", "پیام محلی قدیمی", 300L, "TEXT", null, null, null)
            )
        } finally {
            database.close()
        }
    }
}
