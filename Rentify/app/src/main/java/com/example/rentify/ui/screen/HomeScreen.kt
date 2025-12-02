package com.example.rentify.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rentify.data.local.storage.UserPreferences

/**
 * HomeScreen que verifica autenticación y muestra contenido apropiado
 */
@Composable
fun HomeScreen(
    onGoPropiedades: () -> Unit,
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    // Observar estado de sesión
    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val userName by userPrefs.userName.collectAsStateWithLifecycle(initialValue = null)
    val isDuocVip by userPrefs.isDuocVip.collectAsStateWithLifecycle(initialValue = false)

    // Mostrar contenido según autenticación
    if (isLoggedIn) {
        AuthenticatedHomeScreen(
            userName = userName ?: "Usuario",
            isDuocVip = isDuocVip,
            onGoPropiedades = onGoPropiedades
        )
    } else {
        UnauthenticatedHomeScreen(
            onGoLogin = onGoLogin,
            onGoRegister = onGoRegister
        )
    }
}

/**
 * HomeScreen para usuarios autenticados
 */
@Composable
private fun AuthenticatedHomeScreen(
    userName: String,
    isDuocVip: Boolean,
    onGoPropiedades: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ========== SALUDO PERSONALIZADO ==========
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        "¡Bienvenido, $userName! 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    if (isDuocVip) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "DUOC VIP - 20% descuento",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Tu plataforma de arriendo digital",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ========== ACCIONES RÁPIDAS ==========
            Button(
                onClick = onGoPropiedades,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Explorar Propiedades Cercanas")
            }

            Spacer(Modifier.height(24.dp))

            // ========== INFORMACIÓN DE BENEFICIOS ==========
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "✨ Beneficios Rentify",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    BenefitItem(Icons.Filled.LocationOn, "Búsqueda por ubicación GPS")
                    BenefitItem(Icons.Filled.ViewInAr, "Recorridos virtuales 360°")
                    BenefitItem(Icons.Filled.Verified, "Proceso 100% digital")
                    if (isDuocVip) {
                        BenefitItem(Icons.Filled.Discount, "20% descuento exclusivo DUOC")
                    }
                }
            }
        }
    }
}

/**
 * HomeScreen para usuarios no autenticados (redirige a WelcomeScreen)
 */
@Composable
private fun UnauthenticatedHomeScreen(
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit
) {
    // Mostrar pantalla de bienvenida estilo landing
    WelcomeScreen(
        onGoLogin = onGoLogin,
        onGoRegister = onGoRegister
    )
}

@Composable
private fun BenefitItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}