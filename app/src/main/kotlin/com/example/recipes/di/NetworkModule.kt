package com.example.recipes.di

import com.example.recipes.data.importing.HtmlDownloader
import com.example.recipes.data.importing.OkHttpHtmlDownloader
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ImportingModule {

    @Binds
    abstract fun bindHtmlDownloader(downloader: OkHttpHtmlDownloader): HtmlDownloader
}
