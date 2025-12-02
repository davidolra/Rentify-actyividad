package com.example.rentify.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.ui.viewmodel.RentifyAuthViewModel

/**
 * Pantalla de registro
 */
@Composable
fun RegisterScreenVm(
    vm: RentifyAuthViewModel,
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val state by vm.register.collectAsStateWithLifecycle()

    if (state.success) {
        vm.clearRegisterResult()
        onRegisteredNavigateLogin()
    }

    RegisterScreen(
        pnombre = state.pnombre,
        snombre = state.snombre,
        papellido = state.papellido,
        fechaNacimiento = state.fechaNacimiento,
        email = state.email,
        rut = state.rut,
        telefono = state.telefono,
        pass = state.pass,
        confirm = state.confirm,
        codigoReferido = state.codigoReferido,

        pnombreError = state.pnombreError,
        snombreError = state.snombreError,
        papellidoError = state.papellidoError,
        fechaNacimientoError = state.fechaNacimientoError,
        emailError = state.emailError,
        rutError = state.rutError,
        telefonoError = state.telefonoError,
        passError = state.passError,
        confirmError = state.confirmError,
        codigoReferidoError = state.codigoReferidoError,

        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        isDuocDetected = state.isDuocDetected,

        rolSeleccionado = state.rolSeleccionado ?: "usuario",
        onRolChange = vm::onRolChange,

        onPnombreChange = vm::onPnombreChange,
        onSnombreChange = vm::onSnombreChange,
        onPapellidoChange = vm::onPapellidoChange,
        onFechaNacimientoChange = vm::onFechaNacimientoChange,
        onEmailChange = vm::onRegisterEmailChange,
        onRutChange = vm::onRutChange,
        onTelefonoChange = vm::onTelefonoChange,
        onPassChange = vm::onRegisterPassChange,
        onConfirmChange = vm::onConfirmChange,
        onCodigoReferidoChange = vm::onCodigoReferidoChange,

        onSubmit = vm::submitRegister,
        onGoLogin = onGoLogin
    )
}

@Composable
private fun RegisterScreen(
    pnombre: String,
    snombre: String,
    papellido: String,
    fechaNacimiento: TextFieldValue,
    email: String,
    rut: String,
    telefono: String,
    pass: String,
    confirm: String,
    codigoReferido: String,

    pnombreError: String?,
    snombreError: String?,
    papellidoError: String?,
    fechaNacimientoError: String?,
    emailError: String?,
    rutError: String?,
    telefonoError: String?,
    passError: String?,
    confirmError: String?,
    codigoReferidoError: String?,

    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    isDuocDetected: Boolean,

    rolSeleccionado: String,
    onRolChange: (String) -> Unit,

    onPnombreChange: (String) -> Unit,
    onSnombreChange: (String) -> Unit,
    onPapellidoChange: (String) -> Unit,
    onFechaNacimientoChange: (TextFieldValue) -> Unit,
    onEmailChange: (String) -> Unit,
    onRutChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onCodigoReferidoChange: (String) -> Unit,

    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.background
    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ========== ENCABEZADO ==========
            Text(
                text = "Unete a Rentify",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Encuentra tu hogar ideal de forma simple y segura",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(24.dp))

            // ========== SECCION: DATOS PERSONALES ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Datos Personales",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pnombre,
                        onValueChange = onPnombreChange,
                        label = { Text("Primer Nombre *") },
                        singleLine = true,
                        isError = pnombreError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pnombreError != null) {
                        Text(pnombreError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = snombre,
                        onValueChange = onSnombreChange,
                        label = { Text("Segundo Nombre *") },
                        singleLine = true,
                        isError = snombreError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (snombreError != null) {
                        Text(snombreError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = papellido,
                        onValueChange = onPapellidoChange,
                        label = { Text("Apellido Paterno *") },
                        singleLine = true,
                        isError = papellidoError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (papellidoError != null) {
                        Text(papellidoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fechaNacimiento,
                        onValueChange = onFechaNacimientoChange,
                        label = { Text("Fecha Nacimiento (DD/MM/YYYY) *") },
                        singleLine = true,
                        isError = fechaNacimientoError != null,
                        placeholder = { Text("31/12/2000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (fechaNacimientoError != null) {
                        Text(fechaNacimientoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Debes ser mayor de 18 anos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SECCION: SELECCION DE ROL ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Como usaras Rentify?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(12.dp))

                    // Arrendatario
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = rolSeleccionado == "Arrendatario",
                            onClick = { onRolChange("Arrendatario") }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Busco arrendar",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (rolSeleccionado == "Arrendatario") FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                "Quiero encontrar una propiedad para arrendar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Propietario
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = rolSeleccionado == "Propietario",
                            onClick = { onRolChange("Propietario") }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Publicar propiedades",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (rolSeleccionado == "Propietario") FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                "Quiero publicar mis propiedades en arriendo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SECCION: CONTACTO ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Contacto",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email *") },
                        singleLine = true,
                        isError = emailError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (emailError != null) {
                        Text(emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }

                    if (isDuocDetected) {
                        Spacer(Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Eres DUOC VIP! 20% descuento de por vida en comision de servicio",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rut,
                        onValueChange = onRutChange,
                        label = { Text("RUT *") },
                        singleLine = true,
                        isError = rutError != null,
                        placeholder = { Text("12345678-9") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (rutError != null) {
                        Text(rutError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = onTelefonoChange,
                        label = { Text("Telefono *") },
                        singleLine = true,
                        isError = telefonoError != null,
                        placeholder = { Text("+56912345678") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (telefonoError != null) {
                        Text(telefonoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SECCION: SEGURIDAD ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Seguridad",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = onPassChange,
                        label = { Text("Contrasena *") },
                        singleLine = true,
                        isError = passError != null,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPass) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passError != null) {
                        Text(passError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirm,
                        onValueChange = onConfirmChange,
                        label = { Text("Confirmar Contrasena *") },
                        singleLine = true,
                        isError = confirmError != null,
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirm = !showConfirm }) {
                                Icon(
                                    imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showConfirm) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (confirmError != null) {
                        Text(confirmError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ========== SECCION: CODIGO REFERIDO (OPCIONAL) ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Tienes un codigo de referido? (Opcional)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = codigoReferido,
                        onValueChange = onCodigoReferidoChange,
                        label = { Text("Codigo Referido") },
                        singleLine = true,
                        isError = codigoReferidoError != null,
                        placeholder = { Text("ABC12345") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (codigoReferidoError != null) {
                        Text(codigoReferidoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gana RentifyPoints al registrarte con un codigo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ========== BOTON REGISTRAR ==========
            Button(
                onClick = onSubmit,
                enabled = canSubmit && !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Creando cuenta...")
                } else {
                    Text("Registrarme en Rentify")
                }
            }

            if (errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Ya tengo cuenta - Iniciar Sesion")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}