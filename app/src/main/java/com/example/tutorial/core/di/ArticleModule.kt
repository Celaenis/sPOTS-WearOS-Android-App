package com.example.tutorial.com.example.tutorial.core.di

import com.example.tutorial.data.repository.ArticleRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArticleModule {

    @Provides
    @Singleton
    fun provideArticleRepository(
        firestore: FirebaseFirestore
    ): ArticleRepository = ArticleRepository(firestore)
}
