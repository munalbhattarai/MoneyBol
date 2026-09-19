package com.moneybol.app.core.di

import android.content.Context
import androidx.room.Room
import com.moneybol.app.data.PaymentRepository
import com.moneybol.app.data.PaymentRepositoryImpl
import com.moneybol.app.database.MoneyBolDatabase
import com.moneybol.app.database.PaymentDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MoneyBolDatabase {
        return Room.databaseBuilder(
            context,
            MoneyBolDatabase::class.java,
            "moneybol_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePaymentDao(database: MoneyBolDatabase): PaymentDao {
        return database.paymentDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        impl: PaymentRepositoryImpl
    ): PaymentRepository
}
