package com.example.seniorguard.data.database


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.seniorguard.data.dao.FallEventDao
import com.example.seniorguard.data.model.FallEvent

@Database(entities = [FallEvent::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fallEventDao(): FallEventDao
}
