package com.example.rentify.data.remote

import com.example.rentify.data.remote.api.*
import com.example.rentify.data.remote.dto.UserServiceErrorResponse
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ✅ MEJORADO: Cliente Retrofit optimizado para comunicación con microservicios de Rentify
 *
 * Mejoras:
 * - ✅ Manejo simplificado de fechas LocalDate (yyyy-MM-dd)
 * - ✅ Parseo estructurado de ErrorResponse del backend
 * - ✅ Logging mejorado con información de contexto
 * - ✅ Configuración optimizada de timeouts
 */
object RetrofitClient {

    // ==================== CONFIGURACIÓN DE URLs ====================
    // ⚠️ IMPORTANTE: Cambiar estas URLs según tu entorno
    // Para emulador Android: usar 10.0.2.2 en lugar de localhost
    // Para dispositivo físico: usar la IP de tu PC en la red local
    private const val PC_IP = "192.168.100.7"
    private const val emu_IP = "10.0.2.2"
    private const val BASE_URL_USER_SERVICE = "http://$PC_IP:8081/"
    private const val BASE_URL_PROPERTY_SERVICE = "http://$PC_IP:8082/"
    private const val BASE_URL_DOCUMENT_SERVICE = "http://$PC_IP:8083/"
    private const val BASE_URL_APPLICATION_SERVICE = "http://$PC_IP:8084/"
    private const val BASE_URL_CONTACT_SERVICE = "http://$PC_IP:8085/"
    private const val BASE_URL_REVIEW_SERVICE = "http://$PC_IP:8086/"

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

    // ==================== CONFIGURACIÓN DE GSON SIMPLIFICADA ====================
    // ✅ MEJORADO: Configuración simple y eficiente para fechas LocalDate

    private val gson = GsonBuilder()
        .setLenient()
        // ✅ No necesitamos deserializadores complejos para LocalDate
        // El backend envía fechas como String "yyyy-MM-dd"
        // Gson los maneja automáticamente
        .create()

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
    data class Error(
        val message: String,
        val code: Int? = null,
        val errorResponse: UserServiceErrorResponse? = null  // ✅ NUEVO: Error estructurado
    ) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * ✅ MEJORADO: Extensión para simplificar el manejo de respuestas de Retrofit
 *
 * Mejoras:
 * - Parsea ErrorResponse estructurado del backend
 * - Proporciona mensajes de error más informativos
 * - Maneja casos especiales (401, 404, 500)
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            response.body()?.let {
                ApiResult.Success(it)
            } ?: ApiResult.Error(
                message = "Respuesta vacía del servidor",
                code = response.code()
            )
        } else {
            // ✅ MEJORADO: Intentar parsear ErrorResponse estructurado
            val errorBody = response.errorBody()?.string()
            val errorResponse = parseErrorResponse(errorBody)

            val message = errorResponse?.getUserFriendlyMessage()
                ?: errorBody
                ?: "Error ${response.code()}: ${response.message()}"

            ApiResult.Error(
                message = message,
                code = response.code(),
                errorResponse = errorResponse
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(
            message = getExceptionMessage(e)
        )
    }
}

/**
 * ✅ NUEVO: Parsea el ErrorResponse del backend
 */
private fun parseErrorResponse(errorBody: String?): UserServiceErrorResponse? {
    if (errorBody.isNullOrBlank()) return null

    return try {
        val gson = GsonBuilder().create()
        gson.fromJson(errorBody, UserServiceErrorResponse::class.java)
    } catch (e: JsonSyntaxException) {
        // Si no se puede parsear, retornar null
        android.util.Log.w("RetrofitClient", "No se pudo parsear ErrorResponse: ${e.message}")
        null
    }
}

/**
 * ✅ NUEVO: Obtiene un mensaje de error amigable según el tipo de excepción
 */
private fun getExceptionMessage(e: Exception): String {
    return when {
        e is java.net.UnknownHostException ->
            "Sin conexión a internet. Verifica tu conexión."

        e is java.net.SocketTimeoutException ->
            "Tiempo de espera agotado. El servidor no responde."

        e is java.net.ConnectException ->
            "No se pudo conectar al servidor. Verifica que esté en ejecución."

        e is javax.net.ssl.SSLException ->
            "Error de seguridad en la conexión."

        else ->
            e.message ?: "Error de conexión desconocido"
    }
}