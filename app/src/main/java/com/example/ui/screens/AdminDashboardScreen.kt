package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Alumno
import com.example.data.model.Registro
import com.example.data.repository.DashboardStats
import com.example.ui.theme.ItiNavyDark
import com.example.ui.theme.ItiNavyPrimary
import com.example.ui.theme.ItiOrangeAccent
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralTextPrimary
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.example.ui.viewmodel.ExportState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    stats: DashboardStats,
    registros: List<Registro>,
    alumnos: List<Alumno>,
    searchQuery: String,
    showOnlyToday: Boolean,
    exportState: ExportState,
    onSearchChange: (String) -> Unit,
    onFilterTodayChange: (Boolean) -> Unit,
    onExportExcel: () -> Unit,
    onClearExportState: () -> Unit,
    onUpdateStudent: (oldUid: String, alumno: Alumno, onResult: (Boolean, String?) -> Unit) -> Unit,
    onDeleteStudent: (uid: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    BackHandler { onLogout() }

    var showExcelSuccessDialog by remember { mutableStateOf<Uri?>(null) }
    var studentToEdit by remember { mutableStateOf<Alumno?>(null) }
    var studentToDelete by remember { mutableStateOf<Alumno?>(null) }

    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportState.Success -> {
                showExcelSuccessDialog = exportState.uri
            }
            is ExportState.Error -> {
                snackbarHostState.showSnackbar(exportState.message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_iti),
                                contentDescription = "Logo ITI",
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Panel Administrador",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Bibliotag ITI",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.80f)
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = onExportExcel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .padding(end = 4.dp)
                            .testTag("export_excel_button")
                    ) {
                        if (exportState is ExportState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Excel", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    IconButton(onClick = onLogout, modifier = Modifier.testTag("dashboard_logout_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
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
                .background(NeutralBackground)
        ) {
            // Enterprise Stats Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, NeutralBorder)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "ESTADÍSTICAS DEL SISTEMA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralTextSecondary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCardItem(
                        title = "Entradas Hoy",
                        count = stats.entradasHoy.toString(),
                        icon = Icons.Outlined.Login,
                        accentColor = ItiNavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCardItem(
                        title = "Alumnos Registrados",
                        count = stats.alumnosRegistrados.toString(),
                        icon = Icons.Outlined.Groups,
                        accentColor = ItiNavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCardItem(
                        title = "Total Histórico",
                        count = stats.totalEntradasHistorico.toString(),
                        icon = Icons.Outlined.History,
                        accentColor = ItiNavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = ItiNavyPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Entradas (${registros.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Padrón Alumnos (${alumnos.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            // Filter & Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Buscar alumno, control o carrera...", fontSize = 13.sp, color = NeutralTextSecondary) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = NeutralTextSecondary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Outlined.Clear, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NeutralBorder,
                        focusedBorderColor = ItiNavyPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_search_input")
                )

                if (selectedTab == 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = showOnlyToday,
                        onClick = { onFilterTodayChange(!showOnlyToday) },
                        label = { Text(if (showOnlyToday) "Hoy" else "Histórico", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ItiNavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("filter_today_chip")
                    )
                }
            }

            if (selectedTab == 0) {
                if (registros.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No se encontraron registros coincidentes" else "No hay entradas registradas",
                            color = NeutralTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(registros, key = { it.id }) { registro ->
                            AdminRegistroItem(registro = registro)
                        }
                    }
                }
            } else {
                val filteredAlumnos = remember(alumnos, searchQuery) {
                    if (searchQuery.isBlank()) alumnos
                    else {
                        val q = searchQuery.trim().lowercase()
                        alumnos.filter {
                            it.nombre.lowercase().contains(q) ||
                                    it.numeroControl.lowercase().contains(q) ||
                                    it.carrera.lowercase().contains(q) ||
                                    it.uid.lowercase().contains(q)
                        }
                    }
                }

                if (filteredAlumnos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No hay alumnos dados de alta", color = NeutralTextSecondary, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredAlumnos, key = { it.uid }) { alumno ->
                            AdminAlumnoItem(
                                alumno = alumno,
                                onEdit = { studentToEdit = alumno },
                                onDelete = { studentToDelete = alumno }
                            )
                        }
                    }
                }
            }
        }
    }

    if (studentToEdit != null) {
        val alumno = studentToEdit!!
        EditStudentDialog(
            alumno = alumno,
            onDismiss = { studentToEdit = null },
            onSave = { updatedAlumno ->
                onUpdateStudent(alumno.uid, updatedAlumno) { success, error ->
                    if (success) {
                        scope.launch { snackbarHostState.showSnackbar("Expediente actualizado") }
                        studentToEdit = null
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(error ?: "Error al actualizar") }
                    }
                }
            }
        )
    }

    if (studentToDelete != null) {
        val alumno = studentToDelete!!
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            shape = RoundedCornerShape(12.dp),
            title = { Text("Eliminar Registro", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas dar de baja a ${alumno.nombre} (Ctrl: ${alumno.numeroControl})?", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteStudent(alumno.uid) { success, error ->
                            if (success) {
                                scope.launch { snackbarHostState.showSnackbar("Alumno eliminado") }
                                studentToDelete = null
                            } else {
                                scope.launch { snackbarHostState.showSnackbar(error ?: "Error al eliminar") }
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Eliminar", fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancelar", fontSize = 13.sp, color = NeutralTextSecondary)
                }
            }
        )
    }

    if (showExcelSuccessDialog != null) {
        val uri = showExcelSuccessDialog!!
        AlertDialog(
            onDismissRequest = {
                showExcelSuccessDialog = null
                onClearExportState()
            },
            shape = RoundedCornerShape(12.dp),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = null,
                    tint = ItiNavyPrimary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(text = "Reporte Oficial Generado", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "El archivo .xlsx con las columnas institucionales requeridas se guardó en la carpeta Descargas.",
                    fontSize = 13.sp,
                    color = NeutralTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareExcelFile(context, uri)
                        showExcelSuccessDialog = null
                        onClearExportState()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ItiNavyPrimary),
                    modifier = Modifier.testTag("share_excel_button")
                ) {
                    Icon(imageVector = Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir", fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExcelSuccessDialog = null
                        onClearExportState()
                    }
                ) {
                    Text("Cerrar", fontSize = 13.sp, color = NeutralTextSecondary)
                }
            }
        )
    }
}

@Composable
fun AdminRegistroItem(registro: Registro) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedTime = remember(registro.timestamp) { timeFormat.format(Date(registro.timestamp)) }
    val formattedDate = remember(registro.timestamp) { dateFormat.format(Date(registro.timestamp)) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeutralBorder)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_registro_${registro.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StatusSuccessContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ENTRADA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusSuccess
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = registro.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ctrl: ${registro.numeroControl} • ${registro.carrera}",
                    fontSize = 12.sp,
                    color = NeutralTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ubicación validada por coordenadas",
                    fontSize = 10.sp,
                    color = StatusSuccess,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralTextPrimary
                )
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = NeutralTextSecondary
                )
            }
        }
    }
}

@Composable
fun AdminAlumnoItem(
    alumno: Alumno,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeutralBorder)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_alumno_${alumno.uid}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeutralBackground)
                    .border(1.dp, NeutralBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = ItiNavyPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alumno.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralTextPrimary
                )
                Text(
                    text = "Ctrl: ${alumno.numeroControl} • ${alumno.carrera}",
                    fontSize = 12.sp,
                    color = NeutralTextSecondary
                )
                Text(
                    text = "Tag NFC: ${alumno.uid}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ItiNavyPrimary
                )
            }

            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Editar", tint = NeutralTextSecondary, modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar", tint = StatusError, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStudentDialog(
    alumno: Alumno,
    onDismiss: () -> Unit,
    onSave: (Alumno) -> Unit
) {
    var nombre by remember { mutableStateOf(alumno.nombre) }
    var numeroControl by remember { mutableStateOf(alumno.numeroControl) }
    var uid by remember { mutableStateOf(alumno.uid) }
    var selectedCarrera by remember { mutableStateOf(alumno.carrera) }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        title = { Text("Editar Estudiante y Tarjeta NFC", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = numeroControl,
                    onValueChange = { if (it.all { c -> c.isDigit() }) numeroControl = it },
                    label = { Text("Número de control") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uid,
                    onValueChange = { uid = it.uppercase() },
                    label = { Text("Tag NFC (UID)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = carreraExpanded,
                    onExpandedChange = { carreraExpanded = !carreraExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCarrera,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Carrera") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = carreraExpanded) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = carreraExpanded,
                        onDismissRequest = { carreraExpanded = false }
                    ) {
                        carreras.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c, fontSize = 13.sp) },
                                onClick = {
                                    selectedCarrera = c
                                    carreraExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank() && numeroControl.isNotBlank() && uid.isNotBlank()) {
                        onSave(
                            alumno.copy(
                                nombre = nombre.trim(),
                                numeroControl = numeroControl.trim(),
                                uid = uid.trim().replace(":", "").uppercase(),
                                carrera = selectedCarrera
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ItiNavyPrimary)
            ) {
                Text("Guardar", fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontSize = 13.sp, color = NeutralTextSecondary)
            }
        }
    )
}

@Composable
fun StatCardItem(
    title: String,
    count: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralBackground),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeutralBorder)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = NeutralTextPrimary
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = NeutralTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun shareExcelFile(context: Context, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte de Entradas Bibliotag ITI"))
    } catch (_: Exception) {
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(viewIntent)
        } catch (_: Exception) {}
    }
}
