package com.windrr.boat.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ReceiptEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class BoatDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
}
