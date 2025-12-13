package com.example.rentify.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Chip para mostrar el estado de una solicitud con colores apropiados
 */
@Composable
fun EstadoChip(
    estado: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (estado.uppercase()) {
        "PENDIENTE" -> Pair(
            Color(0xFFFFF3E0),  // Naranja claro
            Color(0xFFE65100)   // Naranja oscuro
        )
        "ACEPTADA", "APROBADA", "APROBADO" -> Pair(
            Color(0xFFE8F5E9),  // Verde claro
            Color(0xFF2E7D32)   // Verde oscuro
        )
        "RECHAZADA", "RECHAZADO" -> Pair(
            Color(0xFFFFEBEE),  // Rojo claro
            Color(0xFFC62828)   // Rojo oscuro
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val displayText = when (estado.uppercase()) {
        "ACEPTADA", "APROBADA", "APROBADO" -> "Aceptada"
        "RECHAZADA", "RECHAZADO" -> "Rechazada"
        "PENDIENTE" -> "Pendiente"
        else -> estado
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = displayText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}