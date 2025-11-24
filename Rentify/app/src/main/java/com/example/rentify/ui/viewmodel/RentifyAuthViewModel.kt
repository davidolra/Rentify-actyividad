package com.example.rentify.ui.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.remote.ApiResult
import com.example.rentify.data.remote.dto.UsuarioRemoteDTO
import com.example.rentify.data.repository.UserRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// ==================== UI STATES ====================

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
    val rolSeleccionado: String? = null,
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

// ==================== VIEWMODEL ====================

class RentifyAuthViewModel(
    private val remoteRepository: UserRemoteRepository
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    private var loggedUser: UsuarioRemoteDTO? = null
    fun getLoggedUser(): UsuarioRemoteDTO? = loggedUser

    // ==================== LOGIN ====================

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

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

            when (val result = remoteRepository.login(s.email.trim(), s.pass)) {
                is ApiResult.Success -> {
                    loggedUser = result.data.usuario
                    _login.update {
                        it.copy(
                            isSubmitting = false,
                            success = true,
                            errorMsg = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _login.update {
                        it.copy(
                            isSubmitting = false,
                            success = false,
                            errorMsg = result.message
                        )
                    }
                }
                is ApiResult.Loading -> { /* No usado aquí */ }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // ==================== REGISTRO ====================

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
                val digitsBeforeCursor =
                    value.text.substring(0, value.selection.start.coerceAtMost(value.text.length))
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

        val newValue = TextFieldValue(text = formatted, selection = androidx.compose.ui.text.TextRange(newCursorPosition))

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

    fun onRolChange(rol: String) {
        _register.update { it.copy(rolSeleccionado = rol) }
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

        val rolValido = !s.rolSeleccionado.isNullOrBlank() &&
                (s.rolSeleccionado == "Arrendatario" || s.rolSeleccionado == "Propietario")

        _register.update { it.copy(canSubmit = noErrors && filled && rolValido) }
    }

    fun submitRegister() {
        val s = _register.value
        if (s.rolSeleccionado.isNullOrBlank()) {
            _register.update { it.copy(errorMsg = "Debes seleccionar un rol") }
            return
        }
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

            // Convertir fecha DD/MM/YYYY a yyyy-MM-dd
            val fechaISO = convertirFechaAISO(s.fechaNacimiento.text)
            if (fechaISO == null) {
                _register.update { it.copy(isSubmitting = false, errorMsg = "Fecha de nacimiento inválida") }
                return@launch
            }

            // Mapear rol a ID
            val rolId = when (s.rolSeleccionado) {
                "Arrendatario" -> 3L
                "Propietario" -> 2L
                else -> 3L
            }

            when (val result = remoteRepository.registrarUsuario(
                pnombre = s.pnombre.trim(),
                snombre = s.snombre.trim(),
                papellido = s.papellido.trim(),
                fnacimiento = fechaISO,
                email = s.email.trim(),
                rut = s.rut.trim(),
                ntelefono = s.telefono.trim(),
                clave = s.pass,
                rolId = rolId
            )) {
                is ApiResult.Success -> {
                    _register.update {
                        it.copy(
                            isSubmitting = false,
                            success = true,
                            errorMsg = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _register.update {
                        it.copy(
                            isSubmitting = false,
                            success = false,
                            errorMsg = result.message
                        )
                    }
                }
                is ApiResult.Loading -> { /* No usado aquí */ }
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    suspend fun getRoleName(rolId: Long?): String {
        return when (rolId) {
            1L -> "Administrador"
            2L -> "Propietario"
            3L -> "Arrendatario"
            else -> "Sin Rol"
        }
    }

    // ==================== HELPERS ====================

    private fun parseFecha(fecha: String): Long? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(fecha)?.time
        } catch (e: Exception) { null }
    }

    private fun convertirFechaAISO(fechaDDMMYYYY: String): String? {
        return try {
            val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdfInput.parse(fechaDDMMYYYY) ?: return null
            sdfOutput.format(date)
        } catch (e: Exception) {
            null
        }
    }

    private fun validateEmail(email: String) = if (email.contains("@")) null else "Email inválido"
    private fun validateName(name: String) = if (name.isNotBlank()) null else "Nombre obligatorio"
    private fun validateRut(rut: String) = if (rut.isNotBlank()) null else "Rut obligatorio"
    private fun validatePhoneChileno(phone: String) = if (phone.isNotBlank()) null else "Teléfono obligatorio"
    private fun validateStrongPassword(pass: String) = if (pass.length >= 8) null else "Mínimo 8 caracteres"
    private fun validateConfirm(pass: String, confirm: String) = if (pass == confirm) null else "No coincide"
    private fun validateCodigoReferido(code: String): String? = null // Opcional
    private fun validateFechaNacimiento(timestamp: Long) = null // Placeholder
}