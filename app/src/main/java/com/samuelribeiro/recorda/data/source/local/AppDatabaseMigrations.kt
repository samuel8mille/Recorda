package com.samuelribeiro.recorda.data.source.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds the local sync command queue (`sync_commands`) and an `updatedAtMillis` column to
 * `topics` and `flashcard_reviews`, used as the conflict-resolution timestamp when those
 * rows are uploaded to the sync backend.
 *
 * Unlike the rest of the schema (which relies on [androidx.room.RoomDatabase.Builder
 * .fallbackToDestructiveMigration]), this migration is non-destructive: wiping
 * `sync_commands` on upgrade would silently drop the user's pending offline changes.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_commands (
                id TEXT NOT NULL PRIMARY KEY,
                commandType TEXT NOT NULL,
                entityId TEXT NOT NULL,
                payloadJson TEXT NOT NULL,
                createdAtMillis INTEGER NOT NULL,
                status TEXT NOT NULL,
                retryCount INTEGER NOT NULL,
                lastErrorMessage TEXT,
                lastAttemptAtMillis INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_commands_status ON sync_commands(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_commands_entityId ON sync_commands(entityId)")
        db.execSQL("ALTER TABLE topics ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE flashcard_reviews ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
    }
}
