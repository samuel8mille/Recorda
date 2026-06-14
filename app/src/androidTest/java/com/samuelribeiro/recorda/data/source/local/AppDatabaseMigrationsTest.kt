package com.samuelribeiro.recorda.data.source.local

import android.content.Context
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that [MIGRATION_7_8] adds the `sync_commands` table and the `updatedAtMillis`
 * columns without touching existing rows in `topics` and `flashcard_reviews`.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationsTest {

    private val dbName = "migration-test-7-8.db"
    private lateinit var helper: SupportSQLiteOpenHelper

    @After
    fun tearDown() {
        helper.close()
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(dbName)
    }

    @Test
    fun migrate7To8_addsSyncCommandsTableAndUpdatedAtColumns_keepingExistingRows() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)

        val v7Helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(7) {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            db.execSQL(
                                """
                                CREATE TABLE topics (
                                    id TEXT NOT NULL PRIMARY KEY,
                                    name TEXT NOT NULL,
                                    flashcardsJson TEXT NOT NULL
                                )
                                """.trimIndent(),
                            )
                            db.execSQL(
                                """
                                CREATE TABLE flashcard_reviews (
                                    id TEXT NOT NULL PRIMARY KEY,
                                    topicId TEXT NOT NULL,
                                    cardIndex INTEGER NOT NULL,
                                    easeFactor REAL NOT NULL,
                                    intervalDays INTEGER NOT NULL,
                                    repetitions INTEGER NOT NULL,
                                    nextReviewAtMillis INTEGER NOT NULL
                                )
                                """.trimIndent(),
                            )
                        }

                        /** Not exercised: this helper only seeds a v7 database. */
                        override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                            Unit
                    },
                )
                .build(),
        )
        v7Helper.writableDatabase.use { db ->
            db.execSQL("INSERT INTO topics (id, name, flashcardsJson) VALUES ('t1', 'Kotlin', '[]')")
            db.execSQL(
                "INSERT INTO flashcard_reviews (id, topicId, cardIndex, easeFactor, intervalDays, repetitions, nextReviewAtMillis) " +
                    "VALUES ('t1_0', 't1', 0, 2.5, 1, 0, 1000)",
            )
        }
        v7Helper.close()

        helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(8) {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            error("Database must already exist at version 7 for this test")
                        }

                        override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                            MIGRATION_7_8.migrate(db)
                        }
                    },
                )
                .build(),
        )

        val db = helper.writableDatabase

        val tableNames = db.query("SELECT name FROM sqlite_master WHERE type = 'table'").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
        }
        assertTrue("sync_commands" in tableNames)

        db.query("SELECT name, flashcardsJson, updatedAtMillis FROM topics WHERE id = 't1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Kotlin", cursor.getString(0))
            assertEquals("[]", cursor.getString(1))
            assertEquals(0L, cursor.getLong(2))
        }

        db.query("SELECT topicId, cardIndex, updatedAtMillis FROM flashcard_reviews WHERE id = 't1_0'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("t1", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
            assertEquals(0L, cursor.getLong(2))
        }

        db.execSQL(
            "INSERT INTO sync_commands (id, commandType, entityId, payloadJson, createdAtMillis, status, retryCount) " +
                "VALUES ('cmd1', 'CREATE_TOPIC', 't1', '{}', 1000, 'PENDING', 0)",
        )
        db.query("SELECT COUNT(*) FROM sync_commands WHERE status = 'PENDING'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }
}
