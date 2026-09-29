package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ItiNavyPrimary
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralTextPrimary
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.StatusError

@Composable
fun NfcSimulationDialog(
    onDismiss: () -> Unit,
    onSimulateScan: (String) -> Unit
) {
    var customUid by remember { mutableStateOf("") }

    val presetStudents = listOf(
        Triple("04A1B2C3", "Alejandro Ruiz Santiago", "Ing. Sistemas (Registrado)"),
        Triple("E2806894", "María Fernanda López", "Ing. Industrial (Registrado)"),
        Triple("1A2B3C4D", "Juan Carlos Toledo", "Ing. Eléctrica (Registrado)"),
        Triple("5E6F7A8B", "Ximena Morales Vásquez", "Gestión Empresarial (Registrado)"),
        Triple("DEADBEEF", "Tag Desconocido", "No registrado (Genera Error)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Nfc,
                    contentDescription = null,
                    tint = ItiNavyPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simulador de Lectura NFC",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralTextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Selecciona una tarjeta para simular el acercamiento al lector:",
                    fontSize = 13.sp,
                    color = NeutralTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetStudents.size) { index ->
                        val (uid, name, desc) = presetStudents[index]
                        val isUnknown = uid == "DEADBEEF"

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnknown) Color(0xFFFEF2F2) else NeutralBackground
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isUnknown) Color(0xFFFCA5A5) else NeutralBorder
                                )
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSimulateScan(uid)
                                    onDismiss()
                                }
                                .testTag("sim_student_$index")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isUnknown) Icons.Outlined.WarningAmber else Icons.Outlined.Badge,
                                    contentDescription = null,
                                    tint = if (isUnknown) StatusError else ItiNavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = NeutralTextPrimary
                                    )
                                    Text(
                                        text = "$desc • $uid",
                                        fontSize = 11.sp,
                                        color = NeutralTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customUid,
                    onValueChange = { customUid = it.uppercase() },
                    placeholder = { Text("Escribe un UID hexadecimal (ej. 04E5F6A7)", fontSize = 12.sp, color = NeutralTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NeutralBorder,
                        focusedBorderColor = ItiNavyPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_uid_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customUid.isNotBlank()) {
                        onSimulateScan(customUid)
                        onDismiss()
                    }
                },
                enabled = customUid.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ItiNavyPrimary),
                modifier = Modifier.testTag("submit_custom_uid")
            ) {
                Text("Escanear", fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_sim_dialog")
            ) {
                Text("Cerrar", fontSize = 13.sp, color = NeutralTextSecondary)
            }
        }
    )
}
