package com.example.seniorguard.di

import android.content.Context
import androidx.room.Room
import com.example.seniorguard.data.dao.FallEventDao
import com.example.seniorguard.data.database.AppDatabase
import com.example.seniorguard.data.repository.GuardianRepository
import com.example.seniorguard.data.repository.GuardianRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule { // 데이터 계층 전반을 다루므로 DataModule이 적절!

    // `@Binds`는 추상(abstract) 함수이므로 abstract class 안에 직접 선언합니다.
    @Binds
    @Singleton
    abstract fun bindGuardianRepository(
        repository: GuardianRepositoryImpl
    ): GuardianRepository

    // `@Provides` 함수들은 구체적인 구현이 필요하므로 companion object 안에 모아둡니다.
    companion object {

        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "senior_guard_db"
            ).build()
        }

        @Provides
        fun provideFallEventDao(appDatabase: AppDatabase): FallEventDao {
            return appDatabase.fallEventDao()
        }
    }
}

