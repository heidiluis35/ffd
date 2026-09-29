package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Alumno
import com.example.data.model.Registro
import com.example.ui.theme.ItiNavyDark
import com.example.ui.theme.ItiNavyPrimary
import com.example.ui.theme.ItiOrangeAccent
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralTextPrimary
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SuccessRegistrationScreen(
    alumno: Alumno,
    registro: Registro,
    countdown: Int,
    onAccept: () -> Unit
) {
    BackHandler {
        onAccept()
    }

    val timeFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-MX")) }
    val timeFormatted = remember(registro.timestamp) { timeFormat.format(Date(registro.timestamp)) }
    val dateFormatted = remember(registro.timestamp) { dateFormat.format(Date(registro.timestamp)).replaceFirstChar { it.uppercase() } }

    val checkScale = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        checkScale.animateTo(1.0f, animationSpec = tween(250))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Icon & Message
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(checkScale.value)
                        .clip(CircleShape)
                        .background(StatusSuccessContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Éxito",
                        tint = StatusSuccess,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Entrada Registrada",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = StatusSuccessContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Login,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACCESO AUTORIZADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Student Credential Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeutralBorder)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_success_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header of digital credential card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ItiNavyPrimary)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_iti),
                                        contentDescription = "Logo ITI",
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "INSTITUTO TECNOLÓGICO DEL ISTMO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Biblioteca Escolar",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Student Information Rows
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "NOMBRE DEL ALUMNO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = alumno.nombre,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralTextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NO. CONTROL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralTextSecondary
                                )
                                Text(
                                    text = alumno.numeroControl,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ItiNavyPrimary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "HORA DE ENTRADA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeutralTextSecondary
                                )
                                Text(
                                    text = timeFormatted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeutralTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "CARRERA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralTextSecondary
                        )
                        Text(
                            text = alumno.carrera,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = NeutralTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ubicación validada por coordenadas oficiales",
                                fontSize = 11.sp,
                                color = StatusSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = NeutralBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "UID: ${registro.alumnoUid}",
                                fontSize = 11.sp,
                                color = NeutralTextSecondary
                            )
                            Text(
                                text = dateFormatted,
                                fontSize = 11.sp,
                                color = NeutralTextSecondary
                            )
                        }
                    }
                }
            }

            // Bottom Auto-return countdown & Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Regresando automáticamente en ${countdown}s...",
                    fontSize = 12.sp,
                    color = NeutralTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ItiNavyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("success_accept_button")
                ) {
                    Text(
                        text = "Aceptar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
