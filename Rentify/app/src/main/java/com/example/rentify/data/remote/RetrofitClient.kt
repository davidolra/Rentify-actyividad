package com.example.rentify.data.remote

import com.example.rentify.data.remote.api.*
import com.example.rentify.data.remote.dto.UserServiceErrorResponse
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para comunicacion con microservicios de Rentify
 */
object RetrofitClient {

    // ==================== CONFIGURACION DE URLs ====================
    private const val PC_IP = "192.168.100.7"
    private const val Fer_IP = "192.168.1.12"
    private const val emu_IP = "10.0.2.2"

    private const val BASE_URL_USER_SERVICE = "http://$Fer_IP:8081/"
    private const val BASE_URL_PROPERTY_SERVICE = "http://$Fer_IP:8082/"
    private const val BASE_URL_DOCUMENT_SERVICE = "http://$Fer_IP:8083/"
    private const val BASE_URL_APPLICATION_SERVICE = "http://$Fer_IP:8084/"
    private const val BASE_URL_CONTACT_SERVICE = "http://$Fer_IP:8085/"
    private const val BASE_URL_REVIEW_SERVICE = "http://$Fer_IP:8086/"

    // ==================== CONFIGURACION DE OKHTTP ====================

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ==================== DESERIALIZADOR DE FECHAS ====================

    /**
     * Deserializador personalizado para manejar multiples formatos de fecha
     * Soporta: ISO 8601 con T, ISO 8601 con espacio, solo fecha
     */
    private class DateDeserializer : JsonDeserializer<Date>, JsonSerializer<Date> {

        private val dateFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        ).onEach {
            it.isLenient = true
            it.timeZone = TimeZone.getDefault()
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Date? {
            if (json == null || json.isJsonNull) return null

            val dateString = json.asString
            if (dateString.isNullOrBlank()) return null

            for (format in dateFormats) {
                try {
                    return format.parse(dateString)
                } catch (e: Exception) {
                    // Intentar siguiente formato
                }
            }

            // Si ninguno funciona, intentar parsear como timestamp
            try {
                return Date(dateString.toLong())
            } catch (e: Exception) {
                android.util.Log.w("DateDeserializer", "No se pudo parsear fecha: $dateString")
                return null
            }
        }

        override fun serialize(
            src: Date?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            if (src == null) return JsonPrimitive("")
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            return JsonPrimitive(format.format(src))
        }
    }

    // ==================== CONFIGURACION DE GSON ====================

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Date::class.java, DateDeserializer())
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()

    // ==================== FUNCION HELPER PARA CREAR RETROFIT ====================

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ==================== INSTANCIAS DE APIS ====================

    val userServiceApi: UserServiceApi by lazy {
        createRetrofit(BASE_URL_USER_SERVICE).create(UserServiceApi::class.java)
    }

    val propertyServiceApi: PropertyServiceApi by lazy {
        createRetrofit(BASE_URL_PROPERTY_SERVICE).create(PropertyServiceApi::class.java)
    }

    val documentServiceApi: DocumentServiceApi by lazy {
        createRetrofit(BASE_URL_DOCUMENT_SERVICE).create(DocumentServiceApi::class.java)
    }

    val applicationServiceApi: ApplicationServiceApi by lazy {
        createRetrofit(BASE_URL_APPLICATION_SERVICE).create(ApplicationServiceApi::class.java)
    }

    val contactServiceApi: ContactServiceApi by lazy {
        createRetrofit(BASE_URL_CONTACT_SERVICE).create(ContactServiceApi::class.java)
    }

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
        val errorResponse: UserServiceErrorResponse? = null
    ) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Extension para simplificar el manejo de respuestas de Retrofit
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            response.body()?.let {
                ApiResult.Success(it)
            } ?: ApiResult.Error(
                message = "Respuesta vacia del servidor",
                code = response.code()
            )
        } else {
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

private fun parseErrorResponse(errorBody: String?): UserServiceErrorResponse? {
    if (errorBody.isNullOrBlank()) return null

    return try {
        val gson = GsonBuilder().create()
        gson.fromJson(errorBody, UserServiceErrorResponse::class.java)
    } catch (e: JsonSyntaxException) {
        android.util.Log.w("RetrofitClient", "No se pudo parsear ErrorResponse: ${e.message}")
        null
    }
}

private fun getExceptionMessage(e: Exception): String {
    return when {
        e is java.net.UnknownHostException ->
            "Sin conexion a internet. Verifica tu conexion."

        e is java.net.SocketTimeoutException ->
            "Tiempo de espera agotado. El servidor no responde."

        e is java.net.ConnectException ->
            "No se pudo conectar al servidor. Verifica que este en ejecucion."

        e is javax.net.ssl.SSLException ->
            "Error de seguridad en la conexion."

        else ->
            e.message ?: "Error de conexion desconocido"
    }
}