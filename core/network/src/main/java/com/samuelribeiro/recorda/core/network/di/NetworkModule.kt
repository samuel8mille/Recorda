package com.samuelribeiro.recorda.core.network.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module providing singleton network infrastructure.
 *
 * Diferença deliberada do UrlShortener: aqui NÃO fazemos certificate pinning.
 * O UrlShortener fixa o pin do servidor próprio (host único, certificado estável);
 * a base URL aqui é a API do Gemini (generativelanguage.googleapis.com), que roda
 * atrás da infraestrutura do Google e roda por um pool de certificados rotativos —
 * fixar o pin quebraria o app a cada rotação sem aviso. TLS padrão (validação pela
 * cadeia confiável do sistema) já é suficiente para esse caso.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CHUCKER_MAX_CONTENT_BYTES = 250_000L

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        @Named("baseUrl") baseUrl: String,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideChuckerInterceptor(
        @ApplicationContext context: Context,
    ): ChuckerInterceptor = ChuckerInterceptor.Builder(context)
        .maxContentLength(CHUCKER_MAX_CONTENT_BYTES)
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(chuckerInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Provides a [HttpLoggingInterceptor] with [HttpLoggingInterceptor.Level.BODY] in debug
     * and [HttpLoggingInterceptor.Level.NONE] in release.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(@Named("debugMode") isDebug: Boolean): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideServiceExecutor(): ServiceExecutor = ServiceExecutor()
}
