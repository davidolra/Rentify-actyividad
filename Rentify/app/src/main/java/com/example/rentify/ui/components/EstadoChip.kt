package com.example.rentify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ✅ COMPONENTE: Chip para mostrar estados de solicitudes con colores
 */
@Composable
fun EstadoChip(
    estado: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, emoji) = when (estado.uppercase()) {
        "PENDIENTE" -> Triple(
            Color(0xFFFFF9C4),  // Amarillo claro
            Color(0xFF827717),  // Amarillo oscuro
            "⏳"
        )
        "ACEPTADA", "ACEPTADO", "APROBADO" -> Triple(
            Color(0xFFC8E6C9),  // Verde claro
            Color(0xFF2E7D32),  // Verde oscuro
            "✅"
        )
        "RECHAZADA", "RECHAZADO" -> Triple(
            Color(0xFFFFCDD2),  // Rojo claro
            Color(0xFFC62828),  // Rojo oscuro
            "❌"
        )
        else -> Triple(
            Color(0xFFE0E0E0),  // Gris claro
            Color(0xFF424242),  // Gris oscuro
            "ℹ️"
        )
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 12.sp
            )
            Text(
                text = estado.uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = textColor,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * ✅ PREVIEW: Para visualizar en Android Studio
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun EstadoChipPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EstadoChip(estado = "PENDIENTE")
            EstadoChip(estado = "ACEPTADA")
            EstadoChip(estado = "RECHAZADA")
            EstadoChip(estado = "DESCONOCIDO")
        }
    }
}