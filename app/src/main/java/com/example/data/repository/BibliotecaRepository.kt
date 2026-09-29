package com.example.data.repository

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.Alumno
import com.example.data.model.Registro
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

// ==========================================
// CONSTANTES DE UBICACIÓN DE LA BIBLIOTECA
// ==========================================
// Coordenadas de referencia: Calle Cuauhtémoc esquina Santos Degollado, Juchitán de Zaragoza, Oaxaca.
// IMPORTANTE: verificar y ajustar estos dos números con Google Maps antes de usar en producción.
// Clave de API de Google Maps: En res/values/strings.xml (@string/google_maps_key) o AndroidManifest.xml
const val BIBLIOTECA_LAT = 16.4325
const val BIBLIOTECA_LNG = -95.0225
const val BIBLIOTECA_RADIO_METROS = 50.0
// ==========================================

data class DashboardStats(
    val entradasHoy: Int = 0,
    val alumnosRegistrados: Int = 0,
    val totalEntradasHistorico: Int = 0
)

class StudentNotFoundException(val uid: String) : Exception("Credencial no vinculada a ningún alumno")
class OutsideRadiusException(val distanceMeters: Double) : Exception("Debes estar dentro de la biblioteca para registrar tu entrada (Estás a ${distanceMeters.toInt()} m)")

class BibliotecaRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val alumnoDao = db.alumnoDao()
    private val registroDao = db.registroDao()

    private val prefs = context.getSharedPreferences("bibliotag_iti_prefs", Context.MODE_PRIVATE)

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    init {
        initFirebaseIfAvailable()
        coroutineScope.launch {
            seedSampleDataIfEmpty()
            listenToFirestoreSync()
        }
    }

    private fun initFirebaseIfAvailable() {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                auth = FirebaseAuth.getInstance()
                Log.d("BibliotecaRepo", "Firebase initialized successfully")
            } else {
                Log.w("BibliotecaRepo", "Firebase not initialized in this environment, using local persistence")
            }
        } catch (e: Exception) {
            Log.e("BibliotecaRepo", "Firebase init error: ${e.message}")
        }
    }

    private suspend fun seedSampleDataIfEmpty() {
        val sampleStudents = listOf(
            Alumno(
                uid = "04A1B2C3",
                numeroControl = "20080123",
                nombre = "Alejandro Ruiz Santiago",
                carrera = "Ing. en Sistemas Computacionales"
            ),
            Alumno(
                uid = "E2806894",
                numeroControl = "21080456",
                nombre = "María Fernanda López Gómez",
                carrera = "Ing. Industrial"
            ),
            Alumno(
                uid = "1A2B3C4D",
                numeroControl = "19080789",
                nombre = "Juan Carlos Toledo Marín",
                carrera = "Ing. Eléctrica"
            ),
            Alumno(
                uid = "5E6F7A8B",
                numeroControl = "22080312",
                nombre = "Ximena Morales Vásquez",
                carrera = "Ing. en Gestión Empresarial"
            ),
            Alumno(
                uid = "9C8B7A6F",
                numeroControl = "20080991",
                nombre = "Diego Armando Pineda Cruz",
                carrera = "Ing. Electrónica"
            )
        )

        for (student in sampleStudents) {
            val existing = alumnoDao.getAlumnoByUid(student.uid)
            if (existing == null) {
                alumnoDao.insertAlumno(student)
                firestore?.collection("alumnos")?.document(student.uid)?.set(student.toMap())
            }
        }
    }

    private fun listenToFirestoreSync() {
        val fs = firestore ?: return

        try {
            fs.collection("alumnos").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                coroutineScope.launch {
                    val remoteAlumnos = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        Alumno.fromMap(data)
                    }
                    if (remoteAlumnos.isNotEmpty()) {
                        alumnoDao.insertAll(remoteAlumnos)
                    }
                }
            }

            fs.collection("registros")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(300)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    coroutineScope.launch {
                        val remoteRegistros = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            Registro.fromMap(doc.id, data)
                        }
                        if (remoteRegistros.isNotEmpty()) {
                            registroDao.insertAll(remoteRegistros)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("BibliotecaRepo", "Firestore sync listener error: ${e.message}")
        }
    }

    private fun getStartOfDayMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun calculateDistanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0].toDouble()
    }

    suspend fun getAlumnoByUid(rawUid: String): Alumno? = withContext(Dispatchers.IO) {
        val cleanUid = rawUid.trim().replace(":", "").uppercase()
        var alumno = alumnoDao.getAlumnoByUid(cleanUid)
        if (alumno == null && firestore != null) {
            try {
                val doc = firestore!!.collection("alumnos").document(cleanUid).get().await()
                if (doc.exists() && doc.data != null) {
                    alumno = Alumno.fromMap(doc.data!!)
                    alumnoDao.insertAlumno(alumno)
                }
            } catch (e: Exception) {
                Log.w("BibliotecaRepo", "Error searching student: ${e.message}")
            }
        }
        alumno
    }

    suspend fun getAlumnoByNumeroControl(control: String): Alumno? = withContext(Dispatchers.IO) {
        val cleanControl = control.trim()
        alumnoDao.getAlumnoByNumeroControl(cleanControl)
    }

    /**
     * Registra SOLO ENTRADA para el alumno validando la distancia GPS hacia la biblioteca.
     */
    suspend fun recordEntrance(
        rawUid: String,
        userLat: Double,
        userLng: Double
    ): Result<Pair<Alumno, Registro>> = withContext(Dispatchers.IO) {
        val cleanUid = rawUid.trim().replace(":", "").uppercase()
        val alumno = getAlumnoByUid(cleanUid) ?: return@withContext Result.failure(StudentNotFoundException(cleanUid))

        val distanceMeters = calculateDistanceMeters(userLat, userLng, BIBLIOTECA_LAT, BIBLIOTECA_LNG)
        if (distanceMeters > BIBLIOTECA_RADIO_METROS) {
            return@withContext Result.failure(OutsideRadiusException(distanceMeters))
        }

        val newRegistro = Registro(
            alumnoUid = cleanUid,
            numeroControl = alumno.numeroControl,
            nombre = alumno.nombre,
            carrera = alumno.carrera,
            timestamp = System.currentTimeMillis(),
            tipo = "entrada", // SOLO ENTRADA
            latitude = userLat,
            longitude = userLng,
            distanciaMetros = distanceMeters
        )

        registroDao.insertRegistro(newRegistro)

        firestore?.let { fs ->
            try {
                fs.collection("registros").document(newRegistro.id).set(newRegistro.toMap())
            } catch (e: Exception) {
                Log.w("BibliotecaRepo", "Firestore write error: ${e.message}")
            }
        }

        saveLocalStudentControl(alumno.numeroControl)
        Result.success(Pair(alumno, newRegistro))
    }

    suspend fun registerAlumno(alumno: Alumno): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val formattedUid = alumno.uid.trim().replace(":", "").uppercase()
            val cleanControl = alumno.numeroControl.trim()

            // 1. Check if control number already registered
            val existingControl = alumnoDao.getAlumnoByNumeroControl(cleanControl)
            if (existingControl != null && existingControl.uid != formattedUid) {
                return@withContext Result.failure(IllegalArgumentException("Este número de control ya está registrado"))
            }

            // 2. Check if UID already linked to another student
            val existingTag = alumnoDao.getAlumnoByUid(formattedUid)
            if (existingTag != null && existingTag.numeroControl != cleanControl) {
                return@withContext Result.failure(IllegalArgumentException("Esta tarjeta NFC ya está vinculada a otro alumno (${existingTag.nombre})"))
            }

            val cleanAlumno = alumno.copy(
                uid = formattedUid,
                numeroControl = cleanControl,
                nombre = alumno.nombre.trim(),
                carrera = alumno.carrera.trim()
            )

            alumnoDao.insertAlumno(cleanAlumno)
            firestore?.collection("alumnos")?.document(formattedUid)?.set(cleanAlumno.toMap())?.await()

            saveLocalStudentControl(cleanControl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAlumno(oldUid: String, newAlumno: Alumno): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanOldUid = oldUid.trim().replace(":", "").uppercase()
            val cleanNewUid = newAlumno.uid.trim().replace(":", "").uppercase()

            if (cleanOldUid != cleanNewUid) {
                // Check if new UID is already used
                val existingTag = alumnoDao.getAlumnoByUid(cleanNewUid)
                if (existingTag != null && existingTag.numeroControl != newAlumno.numeroControl) {
                    return@withContext Result.failure(IllegalArgumentException("El nuevo UID ya pertenece a otro alumno"))
                }
                alumnoDao.deleteAlumno(cleanOldUid)
                firestore?.collection("alumnos")?.document(cleanOldUid)?.delete()
            }

            val formatted = newAlumno.copy(uid = cleanNewUid)
            alumnoDao.insertAlumno(formatted)
            firestore?.collection("alumnos")?.document(cleanNewUid)?.set(formatted.toMap())?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAlumno(rawUid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanUid = rawUid.trim().replace(":", "").uppercase()
            alumnoDao.deleteAlumno(cleanUid)
            firestore?.collection("alumnos")?.document(cleanUid)?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllRegistrosFlow(): Flow<List<Registro>> = registroDao.getAllRegistros()

    fun getRegistrosTodayFlow(): Flow<List<Registro>> = registroDao.getRegistrosToday(getStartOfDayMillis())

    fun getAllAlumnosFlow(): Flow<List<Alumno>> = alumnoDao.getAllAlumnos()

    fun getDashboardStats(): Flow<DashboardStats> {
        val startOfDay = getStartOfDayMillis()
        return combine(
            registroDao.getRegistrosToday(startOfDay),
            registroDao.getAllRegistros(),
            alumnoDao.getAllAlumnos()
        ) { todayList, allList, alumnos ->
            DashboardStats(
                entradasHoy = todayList.size,
                alumnosRegistrados = alumnos.size,
                totalEntradasHistorico = allList.size
            )
        }
    }

    fun getLocalRegisteredControl(): String {
        return prefs.getString("registered_control", "") ?: ""
    }

    fun saveLocalStudentControl(control: String) {
        prefs.edit().putString("registered_control", control).apply()
    }

    suspend fun loginAdmin(email: String, pass: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim()
            val isDemoAdmin = (cleanEmail.equals("admin@itistmo.edu.mx", ignoreCase = true) && pass == "admin123") ||
                    (cleanEmail.equals("admin", ignoreCase = true) && pass == "admin")

            if (isDemoAdmin) {
                return@withContext Result.success(true)
            }

            if (auth != null) {
                auth!!.signInWithEmailAndPassword(cleanEmail, pass).await()
                Result.success(true)
            } else {
                Result.success(isDemoAdmin)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
