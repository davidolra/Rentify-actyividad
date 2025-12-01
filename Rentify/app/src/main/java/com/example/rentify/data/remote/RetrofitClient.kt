package com.example.rentify.data.remote

import com.example.rentify.data.remote.api.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para comunicación con microservicios de Rentify
 * ✅ MEJORADO: Manejo robusto de fechas y logging detallado
 */
object RetrofitClient {

    // ==================== CONFIGURACIÓN DE URLs ====================
    // ⚠️ IMPORTANTE: Cambiar estas URLs según tu entorno
    // Para emulador Android: usar 10.0.2.2 en lugar de localhost
    // Para dispositivo físico: usar la IP de tu PC en la red local

    private const val BASE_URL_USER_SERVICE = "http://10.0.2.2:8081/"
    private const val BASE_URL_PROPERTY_SERVICE = "http://10.0.2.2:8082/"
    private const val BASE_URL_DOCUMENT_SERVICE = "http://10.0.2.2:8083/"
    private const val BASE_URL_APPLICATION_SERVICE = "http://10.0.2.2:8084/"
    private const val BASE_URL_CONTACT_SERVICE = "http://10.0.2.2:8085/"
    private const val BASE_URL_REVIEW_SERVICE = "http://10.0.2.2:8086/"

    // ==================== CONFIGURACIÓN DE OKHTTP ====================

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ==================== CONFIGURACIÓN DE GSON MEJORADA ====================
    // ✅ MEJORADO: Manejo robusto de fechas con múltiples formatos

    private val gson = GsonBuilder()
        .setLenient()
        // ✅ Deserializador personalizado para manejar múltiples formatos de fecha
        .registerTypeAdapter(Date::class.java, JsonDeserializer<Date> { json, _, _ ->
            val dateString = json.asString
            parseDateSafely(dateString)
        })
        // ✅ Serializador para enviar fechas al backend en formato ISO 8601
        .registerTypeAdapter(Date::class.java, JsonSerializer<Date> { date, _, _ ->
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            com.google.gson.JsonPrimitive(formatter.format(date))
        })
        .create()

    /**
     * ✅ NUEVO: Parsea fechas de forma segura intentando múltiples formatos
     */
    private fun parseDateSafely(dateString: String): Date {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",     // ISO 8601 completo con timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSS",      // ISO 8601 completo sin timezone
            "yyyy-MM-dd'T'HH:mm:ss",          // ISO 8601 sin milisegundos
            "yyyy-MM-dd HH:mm:ss",            // Formato SQL
            "yyyy-MM-dd"                       // Solo fecha
        )

        for (format in formats) {
            try {
                val formatter = SimpleDateFormat(format, Locale.US)
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                return formatter.parse(dateString)!!
            } catch (e: Exception) {
                // Intentar siguiente formato
                continue
            }
        }

        // Si ningún formato funciona, retornar fecha actual
        android.util.Log.e("RetrofitClient", "⚠️ No se pudo parsear fecha: $dateString")
        return Date()
    }

    // ==================== FUNCIÓN HELPER PARA CREAR RETROFIT ====================

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ==================== INSTANCIAS DE APIS ====================

    /**
     * User Service API (Puerto 8081)
     * Gestión de usuarios, roles y estados
     */
    val userServiceApi: UserServiceApi by lazy {
        createRetrofit(BASE_URL_USER_SERVICE).create(UserServiceApi::class.java)
    }

    /**
     * Property Service API (Puerto 8082)
     * Gestión de propiedades, fotos, comunas y regiones
     */
    val propertyServiceApi: PropertyServiceApi by lazy {
        createRetrofit(BASE_URL_PROPERTY_SERVICE).create(PropertyServiceApi::class.java)
    }

    /**
     * Document Service API (Puerto 8083)
     * Gestión de documentos de usuarios
     */
    val documentServiceApi: DocumentServiceApi by lazy {
        createRetrofit(BASE_URL_DOCUMENT_SERVICE).create(DocumentServiceApi::class.java)
    }

    /**
     * Application Service API (Puerto 8084)
     * Gestión de solicitudes y registros de arriendo
     */
    val applicationServiceApi: ApplicationServiceApi by lazy {
        createRetrofit(BASE_URL_APPLICATION_SERVICE).create(ApplicationServiceApi::class.java)
    }

    /**
     * Contact Service API (Puerto 8085)
     * Gestión de mensajes de contacto
     */
    val contactServiceApi: ContactServiceApi by lazy {
        createRetrofit(BASE_URL_CONTACT_SERVICE).create(ContactServiceApi::class.java)
    }

    /**
     * Review Service API (Puerto 8086)
     * Gestión de reseñas y valoraciones
     */
    val reviewServiceApi: ReviewServiceApi by lazy {
        createRetrofit(BASE_URL_REVIEW_SERVICE).create(ReviewServiceApi::class.java)
    }
}

/**
 * Clase helper para encapsular resultados de las llamadas API
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Extensión para simplificar el manejo de respuestas de Retrofit
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let {
                ApiResult.Success(it)
            } ?: ApiResult.Error("Respuesta vacía del servidor")
        } else {
            val errorBody = response.errorBody()?.string()
            ApiResult.Error(
                message = errorBody ?: "Error ${response.code()}: ${response.message()}",
                code = response.code()
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(
            message = e.message ?: "Error de conexión desconocido"
        )
    }
}