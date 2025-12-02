package com.example.rentify.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.ui.viewmodel.RentifyAuthViewModel
import com.example.rentify.data.local.storage.UserPreferences
import com.example.rentify.data.local.RentifyDatabase
import com.example.rentify.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LoginScreenVm(
    vm: RentifyAuthViewModel,
    onLoginOkNavigateHome: () -> Unit,
    onGoRegister: () -> Unit
) {
    val state by vm.login.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    // Garantizar que la BD este inicializada
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val db = RentifyDatabase.getInstance(context)
                // Forzar inicializacion verificando que hay roles
                val roles = db.catalogDao().getAllRoles()
                Log.d("LoginScreen", "BD inicializada: ${roles.size} roles encontrados")
            } catch (e: Exception) {
                Log.e("LoginScreen", "Error al inicializar BD: ${e.message}", e)
            }
        }
    }

    // Manejo robusto del exito de login
    LaunchedEffect(state.success) {
        if (state.success) {
            val usuario = vm.getLoggedUser()
            if (usuario != null) {
                try {
                    Log.d("LoginScreen", "Obteniendo rol para usuario: ${usuario.email}")

                    // Obtener nombre del rol (ahora con manejo de errores interno)
                    val rolNombre = vm.getRoleName(usuario.rolId ?: 0L)

                    Log.d("LoginScreen", "Rol obtenido: $rolNombre")

                    // Guardar sesion
                    userPrefs.saveUserSession(
                        userId = usuario.id ?: 0L,
                        email = usuario.email,
                        name = "${usuario.pnombre} ${usuario.papellido}",
                        role = rolNombre,
                        isDuocVip = usuario.duocVip ?: false
                    )

                    Log.d("LoginScreen", "Sesion guardada exitosamente")

                    vm.clearLoginResult()
                    onLoginOkNavigateHome()

                } catch (e: Exception) {
                    // Capturar cualquier error y permitir login con rol por defecto
                    Log.e("LoginScreen", "Error en proceso de login: ${e.message}", e)

                    try {
                        // Intentar guardar con rol por defecto
                        userPrefs.saveUserSession(
                            userId = usuario.id ?: 0L,
                            email = usuario.email,
                            name = "${usuario.pnombre} ${usuario.papellido}",
                            role = "Usuario",
                            isDuocVip = usuario.duocVip ?: false
                        )

                        Log.d("LoginScreen", "Sesion guardada con rol por defecto")

                        vm.clearLoginResult()
                        onLoginOkNavigateHome()
                    } catch (e2: Exception) {
                        Log.e("LoginScreen", "Error critico al guardar sesion: ${e2.message}", e2)
                        // Ultimo recurso: marcar como logged in
                        userPrefs.setLoggedIn(true)
                        vm.clearLoginResult()
                        onLoginOkNavigateHome()
                    }
                }
            } else {
                // Fallback si el usuario es null
                Log.w("LoginScreen", "Usuario null, usando fallback")
                userPrefs.setLoggedIn(true)
                vm.clearLoginResult()
                onLoginOkNavigateHome()
            }
        }
    }

    LoginScreen(
        email = state.email,
        pass = state.pass,
        emailError = state.emailError,
        passError = state.passError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onEmailChange = vm::onLoginEmailChange,
        onPassChange = vm::onLoginPassChange,
        onSubmit = vm::submitLogin,
        onGoRegister = onGoRegister
    )
}

@Composable
private fun LoginScreen(
    email: String,
    pass: String,
    emailError: String?,
    passError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onEmailChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoRegister: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.background
    var showPass by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // =================== LOGO ===================
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de Rentify",
                modifier = Modifier
                    .width(180.dp)
                    .height(180.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Rentify",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tu hogar ideal, 100% digital",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(32.dp))

            // =================== CARD LOGIN ===================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesion",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        singleLine = true,
                        isError = emailError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (emailError != null) {
                        Text(
                            emailError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = onPassChange,
                        label = { Text("Contrasena") },
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPass) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        isError = passError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passError != null) {
                        Text(
                            passError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = onSubmit,
                        enabled = canSubmit && !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Validando...")
                        } else {
                            Text("Iniciar Sesion")
                        }
                    }

                    if (errorMsg != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onGoRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("No tienes cuenta? Registrate")
            }

            Spacer(Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Usuarios @duocuc.cl obtienen 20% descuento en comision de servicio!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}