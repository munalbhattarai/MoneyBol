package com.moneybol.app.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * MoneyBol Room database.
 */
@Database(
    entities = [PaymentEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MoneyBolDatabase : RoomDatabase() {
    abstract fun paymentDao(): PaymentDao
}
