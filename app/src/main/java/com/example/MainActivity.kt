package com.example

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.ErrorUnregisteredScreen
import com.example.ui.screens.InvitationScreen
import com.example.ui.screens.LinkNfcScreen
import com.example.ui.screens.SelfRegisterScreen
import com.example.ui.screens.StudentWaitScreen
import com.example.ui.screens.SuccessRegistrationScreen
import com.example.ui.screens.VerifyLocationScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BibliotecaViewModel
import com.example.ui.viewmodel.BibliotecaViewModelFactory
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private var techListsArray: Array<Array<String>>? = null

    private val viewModel: BibliotecaViewModel by viewModels {
        BibliotecaViewModelFactory((application as BibliotecaApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initNfc()
        processNfcIntent(intent)

        val isNfcReady = try {
            nfcAdapter?.isEnabled == true
        } catch (_: Throwable) {
            false
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BibliotecaAppContent(
                        viewModel = viewModel,
                        isNfcAvailable = isNfcReady
                    )
                }
            }
        }
    }

    private fun initNfc() {
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        } catch (t: Throwable) {
            Log.w("MainActivity", "NfcAdapter not available: ${t.message}")
            nfcAdapter = null
        }

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val techFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        val tagFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        intentFiltersArray = arrayOf(techFilter, ndefFilter, tagFilter)

        techListsArray = arrayOf(
            arrayOf(NfcA::class.java.name, MifareClassic::class.java.name),
            arrayOf(NfcA::class.java.name, MifareUltralight::class.java.name),
            arrayOf(NfcA::class.java.name),
            arrayOf(Ndef::class.java.name),
            arrayOf(IsoDep::class.java.name)
        )
    }

    override fun onResume() {
        super.onResume()
        try {
            val adapter = nfcAdapter
            if (adapter != null && adapter.isEnabled) {
                adapter.enableForegroundDispatch(
                    this,
                    pendingIntent,
                    intentFiltersArray,
                    techListsArray
                )
                Log.d("MainActivity", "NFC Foreground Dispatch enabled")
            }
        } catch (t: Throwable) {
            Log.w("MainActivity", "NFC foreground dispatch error: ${t.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            nfcAdapter?.disableForegroundDispatch(this)
            Log.d("MainActivity", "NFC Foreground Dispatch disabled")
        } catch (t: Throwable) {
            Log.w("MainActivity", "NFC disableForegroundDispatch error: ${t.message}")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processNfcIntent(intent)
    }

    private fun processNfcIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return

        if (action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            tag?.let {
                val rawBytes = it.id
                val hexUid = rawBytes.joinToString("") { byte -> "%02X".format(byte) }
                Log.d("MainActivity", "NFC Tag detected: $hexUid")
                viewModel.onNfcTagDetected(hexUid, this)
            }
        }
    }
}

@Composable
fun BibliotecaAppContent(
    viewModel: BibliotecaViewModel,
    isNfcAvailable: Boolean
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val filteredRegistros by viewModel.filteredRegistros.collectAsState()
    val allAlumnos by viewModel.allAlumnos.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showOnlyToday by viewModel.showOnlyToday.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val showGpsDialog by viewModel.showGpsRequiredDialog.collectAsState()
    val isSimulatedInside by viewModel.isSimulatedInside.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Welcome -> {
                WelcomeScreen(
                    onStartClick = { viewModel.onStartClicked() }
                )
            }

            is Screen.Invitation -> {
                InvitationScreen(
                    onCreateAccountClick = { viewModel.navigateToSelfRegister() },
                    onAlreadyHaveAccountClick = { viewModel.navigateToScanDirectly() },
                    onAdminClick = { viewModel.navigateToAdminLogin() }
                )
            }

            is Screen.SelfRegister -> {
                SelfRegisterScreen(
                    errorMessage = screen.error,
                    onSubmit = { nombre, numeroControl, carrera ->
                        viewModel.onSelfRegisterSubmit(nombre, numeroControl, carrera)
                    },
                    onBack = { viewModel.navigateToInvitation() }
                )
            }

            is Screen.LinkNfc -> {
                LinkNfcScreen(
                    alumno = screen.alumno,
                    errorMessage = screen.error,
                    onTagEntered = { uid -> viewModel.onLinkNfcTag(uid) },
                    onBack = { viewModel.navigateToSelfRegister() }
                )
            }

            is Screen.StudentScan -> {
                StudentWaitScreen(
                    isNfcAvailable = isNfcAvailable,
                    showGpsRequiredDialog = showGpsDialog,
                    onAdminClick = { viewModel.navigateToAdminLogin() },
                    onDismissGpsDialog = { viewModel.dismissGpsDialog() },
                    onSimulateNfcTap = { uid -> viewModel.onNfcTagDetected(uid, context) }
                )
            }

            is Screen.VerifyLocation -> {
                VerifyLocationScreen(
                    alumno = screen.alumno,
                    uid = screen.uid,
                    userLat = screen.userLat,
                    userLng = screen.userLng,
                    isSimulatedInside = isSimulatedInside,
                    errorMessage = screen.errorMessage,
                    onToggleSimulatedLocation = { inside -> viewModel.toggleSimulatedLocation(inside) },
                    onVerifyClick = { alumno, uid, lat, lng ->
                        viewModel.verifyAndRegisterEntrance(alumno, uid, lat, lng)
                    },
                    onBack = { viewModel.returnToScanScreen() }
                )
            }

            is Screen.StudentSuccess -> {
                SuccessRegistrationScreen(
                    alumno = screen.alumno,
                    registro = screen.registro,
                    countdown = countdown,
                    onAccept = { viewModel.returnToScanScreen() }
                )
            }

            is Screen.StudentError -> {
                ErrorUnregisteredScreen(
                    uid = screen.uid,
                    countdown = countdown,
                    onReturn = { viewModel.returnToScanScreen() },
                    onCreateAccount = { viewModel.navigateToSelfRegister() }
                )
            }

            is Screen.AdminLogin -> {
                AdminLoginScreen(
                    isLoading = isLoggingIn,
                    errorMessage = loginError,
                    onLogin = { email, pass -> viewModel.loginAdmin(email, pass) },
                    onBackToKiosk = { viewModel.returnToScanScreen() }
                )
            }

            is Screen.AdminDashboard -> {
                AdminDashboardScreen(
                    stats = stats,
                    registros = filteredRegistros,
                    alumnos = allAlumnos,
                    searchQuery = searchQuery,
                    showOnlyToday = showOnlyToday,
                    exportState = exportState,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterTodayChange = { viewModel.setShowOnlyToday(it) },
                    onExportExcel = { viewModel.exportToExcel(context) },
                    onClearExportState = { viewModel.clearExportState() },
                    onUpdateStudent = { oldUid, alumno, callback ->
                        viewModel.updateStudent(oldUid, alumno, callback)
                    },
                    onDeleteStudent = { uid, callback ->
                        viewModel.deleteStudent(uid, callback)
                    },
                    onLogout = { viewModel.logoutAdmin() }
                )
            }
        }
    }
}
