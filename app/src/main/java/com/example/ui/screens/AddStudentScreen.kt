package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ItiNavyDark
import com.example.ui.theme.ItiNavyPrimary
import com.example.ui.theme.ItiOrangeAccent
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(
    initialUid: String,
    statusMessage: String?,
    onSaveStudent: (uid: String, numControl: String, nombre: String, carrera: String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var uid by remember { mutableStateOf(initialUid) }
    var numeroControl by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }

    val carreras = listOf(
        "Ing. en Sistemas Computacionales",
        "Ing. Industrial",
        "Ing. Eléctrica",
        "Ing. Electrónica",
        "Ing. Mecánica",
        "Ing. Civil",
        "Ing. en Gestión Empresarial",
        "Lic. en Administración",
        "Posgrado / Maestría"
    )

    var carreraExpanded by remember { mutableStateOf(false) }
    var selectedCarrera by remember { mutableStateOf(carreras.first()) }

    var isScanningPromptOpen by remember { mutableStateOf(false) }

    LaunchedEffect(initialUid) {
        if (initialUid.isNotBlank()) {
            uid = initialUid
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alta de Nuevo Alumno",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("add_student_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ItiNavyPrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1F5F9))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Feedback
            if (statusMessage == "SUCCESS") {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Alumno registrado con éxito!",
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "El tag NFC ya está vinculado y listo para marcar asistencia.",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            } else if (statusMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage,
                        color = StatusError,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp
                    )
                }
            }

            // NFC Tag Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ItiOrangeAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = null,
                                    tint = ItiOrangeAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Vinculación de Tag NFC",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ItiNavyDark
                            )
                        }

                        if (uid.isNotBlank()) {
                            Text(
                                text = "TAG DETECTADO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = uid,
                        onValueChange = { uid = it.uppercase() },
                        label = { Text(stringResource(R.string.uid_label)) },
                        placeholder = { Text("Ej. 04A1B2C3") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Sensors, contentDescription = null, tint = ItiNavyPrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_uid_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { isScanningPromptOpen = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ItiNavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_tag_button")
                    ) {
                        Icon(imageVector = Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear Tag Acercando la Credencial")
                    }

                    if (isScanningPromptOpen) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "👉 Acerca la tarjeta NFC al reverso del celular en este momento...",
                            fontSize = 12.sp,
                            color = ItiOrangeAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Student Details Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Datos del Estudiante",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ItiNavyDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nombre Completo
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text(stringResource(R.string.student_name_label)) },
                        placeholder = { Text("Ej. Juan Pérez Santiago") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ItiNavyPrimary)
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Número de Control
                    OutlinedTextField(
                        value = numeroControl,
                        onValueChange = { numeroControl = it },
                        label = { Text(stringResource(R.string.control_number_label)) },
                        placeholder = { Text("Ej. 20080123") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = ItiNavyPrimary)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_control_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Carrera Dropdown
                    ExposedDropdownMenuBox(
                        expanded = carreraExpanded,
                        onExpandedChange = { carreraExpanded = !carreraExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCarrera,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.career_label)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = ItiNavyPrimary)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = carreraExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("student_career_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = carreraExpanded,
                            onDismissRequest = { carreraExpanded = false }
                        ) {
                            carreras.forEach { carrera ->
                                DropdownMenuItem(
                                    text = { Text(carrera, fontSize = 13.sp) },
                                    onClick = {
                                        selectedCarrera = carrera
                                        carreraExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    onSaveStudent(uid, numeroControl, nombre, selectedCarrera)
                },
                enabled = uid.isNotBlank() && numeroControl.isNotBlank() && nombre.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ItiOrangeAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_student_button")
            ) {
                Text(
                    text = "Guardar y Vincular Alumno",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
