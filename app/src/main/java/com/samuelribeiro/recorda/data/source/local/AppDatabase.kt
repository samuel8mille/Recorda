package com.samuelribeiro.recorda.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TopicEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
}
