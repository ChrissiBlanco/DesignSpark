package com.designspark.di

import com.designspark.BuildConfig
import com.designspark.data.remote.api.OpenAiApiService
import com.designspark.data.repository.ProjectRepositoryImpl
import com.designspark.domain.repository.ProjectRepository
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
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
        private const val BASE_URL = "https://api.openai.com/"

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient =
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .addHeader(
                                "Authorization",
                                "Bearer ${BuildConfig.OPENAI_API_KEY}"
                            )
                            .build()
                    )
                }
                // #region agent log
                .addInterceptor { chain ->
                    val response = chain.proceed(chain.request())
                    if (!response.isSuccessful &&
                        response.request.url.host.contains("openai", ignoreCase = true)
                    ) {
                        val snippet = try {
                            response.peekBody(16_384).string()
                        } catch (_: Exception) {
                            "(peek failed)"
                        }
                        Log.w(
                            "AgentDebug",
                            JSONObject().apply {
                                put("sessionId", "a0ce8a")
                                put("timestamp", System.currentTimeMillis())
                                put("hypothesisId", "HTTP")
                                put("location", "AppModule:openAiResponse")
                                put("message", "non_success_response")
                                put("data", JSONObject().apply {
                                    put("code", response.code)
                                    put("bodySnippet", snippet.take(4000))
                                })
                            }.toString()
                        )
                    }
                    response
                }
                // #endregion
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        redactHeader("Authorization")
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
        fun provideOpenAiApiService(retrofit: Retrofit): OpenAiApiService =
            retrofit.create(OpenAiApiService::class.java)

        @Provides
        @Singleton
        fun provideGson(): Gson = Gson()
    }
}
