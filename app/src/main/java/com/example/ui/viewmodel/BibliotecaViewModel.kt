package com.example.ui.viewmodel

import android.content.Context
import android.location.LocationManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.export.ExcelExporter
import com.example.data.model.Alumno
import com.example.data.model.Registro
import com.example.data.repository.BIBLIOTECA_LAT
import com.example.data.repository.BIBLIOTECA_LNG
import com.example.data.repository.BIBLIOTECA_RADIO_METROS
import com.example.data.repository.BibliotecaRepository
import com.example.data.repository.DashboardStats
import com.example.data.repository.OutsideRadiusException
import com.example.data.repository.StudentNotFoundException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Welcome : Screen()
    data object Invitation : Screen()
    data class SelfRegister(val error: String? = null) : Screen()
    data class LinkNfc(val alumno: Alumno, val error: String? = null) : Screen()
    data object StudentScan : Screen()
    data class VerifyLocation(
        val alumno: Alumno,
        val uid: String,
        val userLat: Double,
        val userLng: Double,
        val errorMessage: String? = null
    ) : Screen()
    data class StudentSuccess(val alumno: Alumno, val registro: Registro) : Screen()
    data class StudentError(val uid: String) : Screen()
    data object AdminLogin : Screen()
    data object AdminDashboard : Screen()
}

sealed class ExportState {
    data object Idle : ExportState()
    data object Loading : ExportState()
    data class Success(val uri: Uri, val message: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

class BibliotecaViewModel(private val repository: BibliotecaRepository) : ViewModel() {

    // Always start at Welcome Screen as requested
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _countdown = MutableStateFlow(4)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private var autoReturnJob: Job? = null

    // Check if student already registered on this device
    val hasRegisteredStudentOnDevice: Boolean
        get() = repository.getLocalRegisteredControl().isNotBlank()

    // Real-time registrations and students
    val allRegistros: StateFlow<List<Registro>> = repository.getAllRegistrosFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayRegistros: StateFlow<List<Registro>> = repository.getRegistrosTodayFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlumnos: StateFlow<List<Alumno>> = repository.getAllAlumnosFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<DashboardStats> = repository.getDashboardStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Admin search & filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showOnlyToday = MutableStateFlow(true)
    val showOnlyToday: StateFlow<Boolean> = _showOnlyToday.asStateFlow()

    val filteredRegistros: StateFlow<List<Registro>> = combine(
        allRegistros,
        todayRegistros,
        searchQuery,
        showOnlyToday
    ) { all, today, query, onlyToday ->
        val baseList = if (onlyToday) today else all
        if (query.isBlank()) {
            baseList
        } else {
            val q = query.trim().lowercase()
            baseList.filter {
                it.nombre.lowercase().contains(q) ||
                        it.numeroControl.lowercase().contains(q) ||
                        it.carrera.lowercase().contains(q) ||
                        it.alumnoUid.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Auth State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    // Export State
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // GPS Status dialog trigger
    private val _showGpsRequiredDialog = MutableStateFlow(false)
    val showGpsRequiredDialog: StateFlow<Boolean> = _showGpsRequiredDialog.asStateFlow()

    // Temporary pending scan tag when GPS check runs
    private var pendingScannedUid: String? = null

    // Simulated location for emulator testing (defaults to 8 meters inside library)
    private val _simulatedLat = MutableStateFlow(16.43256)
    val simulatedLat: StateFlow<Double> = _simulatedLat.asStateFlow()

    private val _simulatedLng = MutableStateFlow(-95.02253)
    val simulatedLng: StateFlow<Double> = _simulatedLng.asStateFlow()

    private val _isSimulatedInside = MutableStateFlow(true)
    val isSimulatedInside: StateFlow<Boolean> = _isSimulatedInside.asStateFlow()

    // Draft alumno during self-registration
    private var draftAlumno: Alumno? = null

    fun onStartClicked() {
        if (hasRegisteredStudentOnDevice) {
            _currentScreen.value = Screen.StudentScan
        } else {
            _currentScreen.value = Screen.Invitation
        }
    }

    fun navigateToInvitation() {
        _currentScreen.value = Screen.Invitation
    }

    fun navigateToSelfRegister() {
        _currentScreen.value = Screen.SelfRegister()
    }

    fun navigateToScanDirectly() {
        _currentScreen.value = Screen.StudentScan
    }

    fun onSelfRegisterSubmit(nombre: String, numeroControl: String, carrera: String) {
        if (nombre.trim().split("\\s+".toRegex()).size < 2) {
            _currentScreen.value = Screen.SelfRegister("Ingresa tu nombre y al menos un apellido")
            return
        }
        if (numeroControl.isBlank() || !numeroControl.all { it.isDigit() }) {
            _currentScreen.value = Screen.SelfRegister("El número de control debe contener solo dígitos")
            return
        }

        viewModelScope.launch {
            val existing = repository.getAlumnoByNumeroControl(numeroControl.trim())
            if (existing != null) {
                _currentScreen.value = Screen.SelfRegister("Este número de control ya está registrado")
                return@launch
            }

            val alumno = Alumno(
                uid = "", // to be linked on next screen
                numeroControl = numeroControl.trim(),
                nombre = nombre.trim(),
                carrera = carrera.trim()
            )
            draftAlumno = alumno
            _currentScreen.value = Screen.LinkNfc(alumno)
        }
    }

    fun onLinkNfcTag(rawUid: String) {
        val cleanUid = rawUid.trim().replace(":", "").uppercase()
        val alumno = draftAlumno ?: return

        if (cleanUid.isBlank()) {
            _currentScreen.value = Screen.LinkNfc(alumno, "Ingresa o escanea un UID válido")
            return
        }

        viewModelScope.launch {
            // Check if tag already used by another student
            val existing = repository.getAlumnoByUid(cleanUid)
            if (existing != null && existing.numeroControl != alumno.numeroControl) {
                _currentScreen.value = Screen.LinkNfc(
                    alumno,
                    "Esta tarjeta NFC ya está vinculada a otro alumno (${existing.nombre})"
                )
                return@launch
            }

            val completeAlumno = alumno.copy(uid = cleanUid)
            val result = repository.registerAlumno(completeAlumno)
            result.fold(
                onSuccess = {
                    draftAlumno = null
                    _currentScreen.value = Screen.StudentScan
                },
                onFailure = { ex ->
                    _currentScreen.value = Screen.LinkNfc(alumno, ex.message)
                }
            )
        }
    }

    fun onNfcTagDetected(rawUid: String, context: Context) {
        val cleanUid = rawUid.trim().replace(":", "").uppercase()
        if (cleanUid.isBlank()) return

        when (val current = _currentScreen.value) {
            is Screen.LinkNfc -> {
                onLinkNfcTag(cleanUid)
            }
            is Screen.StudentScan -> {
                handleScanTag(cleanUid, context)
            }
            else -> {
                // If on wait/invitation or any student screen, handle as scan
                handleScanTag(cleanUid, context)
            }
        }
    }

    private fun handleScanTag(uid: String, context: Context) {
        autoReturnJob?.cancel()

        viewModelScope.launch {
            val alumno = repository.getAlumnoByUid(uid)
            if (alumno == null) {
                _currentScreen.value = Screen.StudentError(uid)
                startCountdownAutoReturn(seconds = 6)
                return@launch
            }

            // Check if GPS is enabled on device
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val isGpsOn = lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

            if (!isGpsOn) {
                pendingScannedUid = uid
                _showGpsRequiredDialog.value = true
                return@launch
            }

            // GPS is active, proceed to location verification screen
            _currentScreen.value = Screen.VerifyLocation(
                alumno = alumno,
                uid = uid,
                userLat = _simulatedLat.value,
                userLng = _simulatedLng.value
            )
        }
    }

    fun dismissGpsDialog() {
        _showGpsRequiredDialog.value = false
        pendingScannedUid = null
    }

    fun onGpsActivated(context: Context) {
        _showGpsRequiredDialog.value = false
        val uid = pendingScannedUid ?: return
        handleScanTag(uid, context)
    }

    fun toggleSimulatedLocation(inside: Boolean) {
        _isSimulatedInside.value = inside
        if (inside) {
            // Inside library radius (~8m)
            _simulatedLat.value = 16.43256
            _simulatedLng.value = -95.02253
        } else {
            // Outside library radius (~260m)
            _simulatedLat.value = 16.43480
            _simulatedLng.value = -95.02100
        }

        val current = _currentScreen.value
        if (current is Screen.VerifyLocation) {
            _currentScreen.value = current.copy(
                userLat = _simulatedLat.value,
                userLng = _simulatedLng.value,
                errorMessage = null
            )
        }
    }

    fun verifyAndRegisterEntrance(alumno: Alumno, uid: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val result = repository.recordEntrance(uid, lat, lng)
            result.fold(
                onSuccess = { (savedAlumno, registro) ->
                    _currentScreen.value = Screen.StudentSuccess(savedAlumno, registro)
                    startCountdownAutoReturn(seconds = 5)
                },
                onFailure = { error ->
                    if (error is OutsideRadiusException) {
                        val current = _currentScreen.value
                        if (current is Screen.VerifyLocation) {
                            _currentScreen.value = current.copy(
                                errorMessage = "Debes estar dentro de la biblioteca para registrar tu entrada"
                            )
                        }
                    } else if (error is StudentNotFoundException) {
                        _currentScreen.value = Screen.StudentError(uid)
                    } else {
                        val current = _currentScreen.value
                        if (current is Screen.VerifyLocation) {
                            _currentScreen.value = current.copy(
                                errorMessage = error.message ?: "Error al verificar ubicación"
                            )
                        }
                    }
                }
            )
        }
    }

    private fun startCountdownAutoReturn(seconds: Int = 4) {
        autoReturnJob?.cancel()
        _countdown.value = seconds
        autoReturnJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _countdown.value = i
                delay(1000)
            }
            returnToScanScreen()
        }
    }

    fun returnToScanScreen() {
        autoReturnJob?.cancel()
        _currentScreen.value = Screen.StudentScan
    }

    fun navigateToAdminLogin() {
        autoReturnJob?.cancel()
        _loginError.value = null
        if (_isAdminLoggedIn.value) {
            _currentScreen.value = Screen.AdminDashboard
        } else {
            _currentScreen.value = Screen.AdminLogin
        }
    }

    fun loginAdmin(email: String, pass: String) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _loginError.value = null
            val result = repository.loginAdmin(email, pass)
            _isLoggingIn.value = false
            result.fold(
                onSuccess = { success ->
                    if (success) {
                        _isAdminLoggedIn.value = true
                        _currentScreen.value = Screen.AdminDashboard
                    } else {
                        _loginError.value = "Usuario o contraseña incorrectos"
                    }
                },
                onFailure = {
                    _loginError.value = "Usuario o contraseña incorrectos"
                }
            )
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _currentScreen.value = Screen.StudentScan
    }

    fun navigateToDashboard() {
        _currentScreen.value = Screen.AdminDashboard
    }

    fun updateStudent(oldUid: String, alumno: Alumno, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateAlumno(oldUid, alumno)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { ex -> onResult(false, ex.message) }
            )
        }
    }

    fun deleteStudent(uid: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteAlumno(uid)
            result.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { ex -> onResult(false, ex.message) }
            )
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowOnlyToday(onlyToday: Boolean) {
        _showOnlyToday.value = onlyToday
    }

    fun exportToExcel(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            val records = filteredRegistros.value
            if (records.isEmpty()) {
                _exportState.value = ExportState.Error("No hay registros que coincidan con el filtro actual para exportar")
                return@launch
            }

            val titleSuffix = if (showOnlyToday.value) "Hoy" else "Historico"
            val result = ExcelExporter.exportRegistrosToExcel(context, records, titleSuffix)
            result.fold(
                onSuccess = { uri ->
                    _exportState.value = ExportState.Success(
                        uri = uri,
                        message = "Archivo Excel generado con éxito en Descargas"
                    )
                },
                onFailure = { error ->
                    _exportState.value = ExportState.Error(
                        message = "Error al exportar: ${error.localizedMessage ?: error.message}"
                    )
                }
            )
        }
    }

    fun clearExportState() {
        _exportState.value = ExportState.Idle
    }
}

class BibliotecaViewModelFactory(private val repository: BibliotecaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BibliotecaViewModel::class.java)) {
            return BibliotecaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
