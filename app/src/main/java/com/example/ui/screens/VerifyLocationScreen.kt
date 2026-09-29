package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Alumno
import com.example.data.repository.BIBLIOTECA_LAT
import com.example.data.repository.BIBLIOTECA_LNG
import com.example.data.repository.BIBLIOTECA_RADIO_METROS
import com.example.ui.theme.ItiNavyDark
import com.example.ui.theme.ItiNavyPrimary
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralTextPrimary
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusErrorContainer
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

// ==========================================
// CONSTANTES DE UBICACIÓN DE LA BIBLIOTECA
// ==========================================
// Coordenadas de referencia: Calle Cuauhtémoc esquina Santos Degollado, Juchitán de Zaragoza, Oaxaca.
// IMPORTANTE: verificar y ajustar estos dos números con Google Maps antes de usar en producción.
// Clave de API de Google Maps: En res/values/strings.xml (@string/google_maps_key) o AndroidManifest.xml
// ==========================================

@Composable
fun VerifyLocationScreen(
    alumno: Alumno,
    uid: String,
    userLat: Double,
    userLng: Double,
    isSimulatedInside: Boolean,
    errorMessage: String?,
    onToggleSimulatedLocation: (Boolean) -> Unit,
    onVerifyClick: (Alumno, String, Double, Double) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val bibliotecaLatLng = remember { LatLng(BIBLIOTECA_LAT, BIBLIOTECA_LNG) }
    val userLatLng = remember(userLat, userLng) { LatLng(userLat, userLng) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bibliotecaLatLng, 18.2f)
    }

    // Coordinate validation (distance in meters is internal, never shown as numbers to the user)
    val distanceResults = remember(userLat, userLng) {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            userLat, userLng,
            BIBLIOTECA_LAT, BIBLIOTECA_LNG,
            results
        )
        results[0].toInt()
    }

    val isWithinRadius = distanceResults <= BIBLIOTECA_RADIO_METROS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("verify_location_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = ItiNavyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Validación de ubicación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeutralTextPrimary
                    )
                    Text(
                        text = "${alumno.nombre} • ${alumno.numeroControl}",
                        fontSize = 12.sp,
                        color = NeutralTextSecondary
                    )
                }
            }

            // Google Maps Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeutralBorder)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("google_maps_card")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false
                        )
                    ) {
                        Marker(
                            state = MarkerState(position = bibliotecaLatLng),
                            title = "Biblioteca Escolar ITI",
                            snippet = "Zona autorizada para registro"
                        )

                        Marker(
                            state = MarkerState(position = userLatLng),
                            title = "Tu ubicación",
                            snippet = if (isWithinRadius) "Ubicación confirmada" else "Fuera de coordenadas"
                        )

                        Circle(
                            center = bibliotecaLatLng,
                            radius = BIBLIOTECA_RADIO_METROS,
                            fillColor = Color(0x220D3B66),
                            strokeColor = ItiNavyPrimary,
                            strokeWidth = 2f
                        )
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBorder),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = ItiNavyPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Perímetro Biblioteca ITI",
                                color = NeutralTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Status Card (Without meter numbers: clean corporate confirmation or rejection)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeutralBorder)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isWithinRadius) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = if (isWithinRadius) StatusSuccess else StatusError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isWithinRadius) "Ubicación confirmada" else "Ubicación no autorizada",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWithinRadius) StatusSuccess else StatusError
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isWithinRadius) {
                            "Te encuentras dentro del perímetro oficial de la biblioteca para validar tu entrada."
                        } else {
                            "Debes encontrarte físicamente en las coordenadas de la biblioteca para registrar tu asistencia."
                        },
                        fontSize = 13.sp,
                        color = NeutralTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = StatusErrorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage,
                                color = StatusError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulation selector for developer / emulator verification
                    Text(
                        text = "Modo de simulación (para pruebas en emulador):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeutralTextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = isSimulatedInside,
                            onClick = { onToggleSimulatedLocation(true) },
                            label = { Text("Dentro de la biblioteca", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusSuccessContainer,
                                selectedLabelColor = StatusSuccess
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sim_inside_gps_chip")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = !isSimulatedInside,
                            onClick = { onToggleSimulatedLocation(false) },
                            label = { Text("Fuera de la biblioteca", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusErrorContainer,
                                selectedLabelColor = StatusError
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sim_outside_gps_chip")
                        )
                    }
                }
            }

            // Action Button: Validates coordinates and registers or rejects
            Button(
                onClick = {
                    onVerifyClick(alumno, uid, userLat, userLng)
                },
                enabled = isWithinRadius,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWithinRadius) ItiNavyPrimary else Color(0xFF9CA3AF),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_verify_location_submit")
            ) {
                Text(
                    text = if (isWithinRadius) "Validar y Registrar Entrada" else "Rechazado: fuera de coordenadas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
