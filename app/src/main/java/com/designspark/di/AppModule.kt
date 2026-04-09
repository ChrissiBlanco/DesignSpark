package com.designspark.di

import com.designspark.BuildConfig
import com.designspark.data.remote.api.AnthropicApiService
import com.designspark.data.repository.ProjectRepositoryImpl
import com.designspark.domain.repository.ProjectRepository
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    companion object {
        private const val BASE_URL = "https://api.anthropic.com/"

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient =
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .addHeader("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
                            .addHeader("anthropic-version", "2023-06-01")
                            .build()
                    )
                }
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                        else HttpLoggingInterceptor.Level.NONE
                    }
                )
                .build()

        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        @Provides
        @Singleton
        fun provideAnthropicApiService(retrofit: Retrofit): AnthropicApiService =
            retrofit.create(AnthropicApiService::class.java)

        @Provides
        @Singleton
        fun provideGson(): Gson = Gson()
    }
}
