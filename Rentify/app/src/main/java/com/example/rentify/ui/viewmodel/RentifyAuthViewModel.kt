package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.rentify.domain.validation.*
import com.example.rentify.data.repository.RentifyUserRepository
import com.example.rentify.data.local.entities.UsuarioEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

// ========== ESTADOS DE UI ==========

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class RegisterUiState(
    // Datos personales
    val pnombre: String = "",
    val snombre: String = "",
    val papellido: String = "",
    val fechaNacimiento: TextFieldValue = TextFieldValue(""),
    val email: String = "",
    val rut: String = "",
    val telefono: String = "",
    val pass: String = "",
    val confirm: String = "",
    val codigoReferido: String = "",
    val rolSeleccionado: String = "Inquilino",

    // Errores
    val pnombreError: String? = null,
    val snombreError: String? = null,
    val papellidoError: String? = null,
    val fechaNacimientoError: String? = null,
    val emailError: String? = null,
    val rutError: String? = null,
    val telefonoError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val codigoReferidoError: String? = null,

    // Control
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
    val isDuocDetected: Boolean = false
)

class RentifyAuthViewModel(
    private val repository: RentifyUserRepository
) : ViewModel() {

    // Flujos de estado
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    // Usuario logueado actual
    private var loggedUser: UsuarioEntity? = null

    fun getLoggedUser(): UsuarioEntity? = loggedUser

    // ========== LOGIN ==========

    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    // ✅ FIX: Eliminar delay y mejorar flujo
    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

            try {
                val result = repository.login(s.email.trim(), s.pass)

                _login.update {
                    if (result.isSuccess) {
                        loggedUser = result.getOrNull()
                        it.copy(isSubmitting = false, success = true, errorMsg = null)
                    } else {
                        it.copy(
                            isSubmitting = false,
                            success = false,
                            errorMsg = result.exceptionOrNull()?.message ?: "Credenciales inválidas"
                        )
                    }
                }
            } catch (e: Exception) {
                _login.update {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = "Error de conexión: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // ========== REGISTRO ==========

    fun onPnombreChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(pnombre = filtered, pnombreError = validateName(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onSnombreChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(snombre = filtered, snombreError = validateName(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onPapellidoChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(papellido = filtered, papellidoError = validateName(filtered)) }
        recomputeRegisterCanSubmit()
    }

    suspend fun getRoleName(rolId: Long?): String {
        return repository.getRoleName(rolId)
    }

    fun onFechaNacimientoChange(value: TextFieldValue) {
        val soloDigitos = value.text.filter { it.isDigit() }.take(8)

        val formatted = buildString {
            soloDigitos.forEachIndexed { index, char ->
                if (index == 2 || index == 4) append('/')
                append(char)
            }
        }

        val newCursorPosition = when {
            formatted.isEmpty() -> 0
            formatted.length <= value.selection.start -> formatted.length
            else -> {
                val digitsBeforeCursor = value.text.substring(0, value.selection.start.coerceAtMost(value.text.length))
                    .count { it.isDigit() }

                var pos = 0
                var digitCount = 0
                for (i in formatted.indices) {
                    if (formatted[i].isDigit()) {
                        digitCount++
                        if (digitCount >= digitsBeforeCursor) {
                            pos = i + 1
                            break
                        }
                    }
                }
                pos.coerceIn(0, formatted.length)
            }
        }

        val newValue = TextFieldValue(
            text = formatted,
            selection = TextRange(newCursorPosition)
        )

        val error = if (formatted.length == 10) {
            val timestamp = parseFecha(formatted)
            if (timestamp != null) validateFechaNacimiento(timestamp) else "Fecha inválida"
        } else if (formatted.isNotEmpty()) {
            "Formato: DD/MM/YYYY"
        } else {
            null
        }

        _register.update { it.copy(fechaNacimiento = newValue, fechaNacimientoError = error) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        val error = validateEmail(value)
        val isDuoc = value.trim().lowercase().let { it.endsWith("@duoc.cl") || it.endsWith("@duocuc.cl") }
        _register.update { it.copy(email = value, emailError = error, isDuocDetected = isDuoc) }
        recomputeRegisterCanSubmit()
    }

    fun onRutChange(value: String) {
        val filtered = value.filter { it.isDigit() || it.lowercaseChar() == 'k' || it == '.' || it == '-' }
            .take(12)
        _register.update { it.copy(rut = filtered, rutError = validateRut(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onTelefonoChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '+' || it == ' ' }.take(15)
        _register.update { it.copy(telefono = filtered, telefonoError = validatePhoneChileno(filtered)) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    fun onCodigoReferidoChange(value: String) {
        val filtered = value.uppercase().filter { it.isLetterOrDigit() }.take(8)
        _register.update { it.copy(codigoReferido = filtered, codigoReferidoError = validateCodigoReferido(filtered)) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value

        val errors = listOf(
            s.pnombreError, s.snombreError, s.papellidoError,
            s.fechaNacimientoError, s.emailError, s.rutError,
            s.telefonoError, s.passError, s.confirmError, s.codigoReferidoError
        )

        val noErrors = errors.all { it == null }

        val filled = s.pnombre.isNotBlank() && s.snombre.isNotBlank() &&
                s.papellido.isNotBlank() && s.fechaNacimiento.text.length == 10 &&
                s.email.isNotBlank() && s.rut.isNotBlank() &&
                s.telefono.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()

        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    // ✅ FIX: Eliminar delay y mejorar manejo de errores
    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

            try {
                val fechaNacimientoTimestamp = parseFecha(s.fechaNacimiento.text)
                if (fechaNacimientoTimestamp == null) {
                    _register.update {
                        it.copy(
                            isSubmitting = false,
                            errorMsg = "Fecha de nacimiento inválida"
                        )
                    }
                    return@launch
                }

                val result = repository.register(
                    pnombre = s.pnombre.trim(),
                    snombre = s.snombre.trim(),
                    papellido = s.papellido.trim(),
                    fnacimiento = fechaNacimientoTimestamp,
                    email = s.email.trim(),
                    rut = s.rut.trim(),
                    ntelefono = s.telefono.trim(),
                    password = s.pass,
                    rolSeleccionado = s.rolSeleccionado
                )

                _register.update {
                    if (result.isSuccess) {
                        it.copy(isSubmitting = false, success = true, errorMsg = null)
                    } else {
                        it.copy(
                            isSubmitting = false,
                            success = false,
                            errorMsg = result.exceptionOrNull()?.message ?: "No se pudo registrar"
                        )
                    }
                }
            } catch (e: Exception) {
                _register.update {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = "Error al registrar: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    // ========== UTILIDADES ==========

    private fun parseFecha(fecha: String): Long? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(fecha)
            date?.time
        } catch (e: Exception) {
            null
        }
    }

    fun onRolChange(rol: String) {
        _register.update { it.copy(rolSeleccionado = rol) }
    }
}