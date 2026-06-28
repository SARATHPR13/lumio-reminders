package com.lumio.app.di

import android.content.Context
import com.lumio.app.data.local.dao.CategoryDao
import com.lumio.app.data.local.dao.ReminderDao
import com.lumio.app.data.local.database.LumioDatabase
import com.lumio.app.data.repository.CategoryRepositoryImpl
import com.lumio.app.data.repository.ReminderRepositoryImpl
import com.lumio.app.domain.repository.CategoryRepository
import com.lumio.app.domain.repository.ReminderRepository
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): LumioDatabase = LumioDatabase.buildDatabase(context)

    @Provides
    @Singleton
    fun provideReminderDao(db: LumioDatabase): ReminderDao = db.reminderDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: LumioDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideReminderRepository(
        impl: ReminderRepositoryImpl
    ): ReminderRepository = impl

    @Provides
    @Singleton
    fun provideCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository = impl
}
