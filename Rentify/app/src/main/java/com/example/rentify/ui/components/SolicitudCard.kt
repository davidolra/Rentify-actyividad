package com.example.rentify.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rentify.ui.viewmodel.SolicitudConDatos
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ COMPONENTE: Card para mostrar una solicitud con datos enriquecidos del backend
 */
@Composable
fun SolicitudCard(
    solicitudConDatos: SolicitudConDatos,
    onClick: () -> Unit = {},
    onActualizarEstado: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // FILA 1: Título y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título de la propiedad
                Text(
                    text = solicitudConDatos.tituloPropiedad ?: "Propiedad",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Chip de estado
                EstadoChip(estado = solicitudConDatos.nombreEstado ?: "PENDIENTE")
            }

            // FILA 2: Código y Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Código de propiedad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Código",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = solicitudConDatos.codigoPropiedad ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Fecha de solicitud
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Fecha",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    val fecha = Date(solicitudConDatos.solicitud.fsolicitud)
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text(
                        text = formatter.format(fecha),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // FILA 3: Precio (si está disponible)
            solicitudConDatos.precioMensual?.let { precio ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Precio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
                    Text(
                        text = "${formatter.format(precio)}/mes",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // DIVIDER
            if (onActualizarEstado != null && solicitudConDatos.nombreEstado == "PENDIENTE") {
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // BOTONES DE ACCIÓN (solo para PENDIENTE y si se proporciona callback)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Aceptar
                    OutlinedButton(
                        onClick = { onActualizarEstado("ACEPTADA") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Aceptar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aceptar")
                    }

                    // Botón Rechazar
                    OutlinedButton(
                        onClick = { onActualizarEstado("RECHAZADA") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Rechazar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}

/**
 * ✅ PREVIEW: Para visualizar en Android Studio
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SolicitudCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Solicitud pendiente
            SolicitudCard(
                solicitudConDatos = com.example.rentify.ui.viewmodel.SolicitudConDatos(
                    solicitud = com.example.rentify.data.local.entities.SolicitudEntity(
                        id = 1,
                        fsolicitud = System.currentTimeMillis(),
                        total = 0,
                        usuarios_id = 1,
                        estado_id = 1,
                        propiedad_id = 7
                    ),
                    tituloPropiedad = "Departamento 2D/2B Amoblado - Providencia",
                    codigoPropiedad = "DP007",
                    nombreEstado = "PENDIENTE",
                    precioMensual = 650000.0
                ),
                onActualizarEstado = {}
            )

            // Solicitud aceptada
            SolicitudCard(
                solicitudConDatos = com.example.rentify.ui.viewmodel.SolicitudConDatos(
                    solicitud = com.example.rentify.data.local.entities.SolicitudEntity(
                        id = 2,
                        fsolicitud = System.currentTimeMillis(),
                        total = 0,
                        usuarios_id = 1,
                        estado_id = 2,
                        propiedad_id = 8
                    ),
                    tituloPropiedad = "Casa 3D/2B con Jardín",
                    codigoPropiedad = "CS008",
                    nombreEstado = "ACEPTADA",
                    precioMensual = 850000.0
                )
            )
        }
    }
}