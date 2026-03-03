package com.example.tutorial.com.example.tutorial.presentation.feature.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tutorial.com.example.tutorial.presentation.feature.report.ReportViewModel
import com.example.tutorial.com.example.tutorial.presentation.components.ScreenScaffold
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInfoScreen(
    vm: ProfileViewModel = hiltViewModel(),
    reportVm: ReportViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val calState = remember { UseCaseState() }
    var pickedRange by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }
    CalendarDialog(
        state = calState,
        selection = CalendarSelection.Period { s, e ->
            pickedRange = s to e
        },
        config = CalendarConfig(yearSelection = true, monthSelection = true)
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { destUri: Uri? ->
        if (destUri != null && pickedRange != null && ui.profile != null) {

            ctx.contentResolver.takePersistableUriPermission(
                destUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val (from, to) = pickedRange!!
            val profile = ui.profile!!

            reportVm.buildPdf(
                patient = Triple(profile.displayName, profile.age, profile.sex),
                from = from,
                to = to,
                dest = destUri,
                resolver = ctx.contentResolver,
                onReady = { pdfUri ->
                    val open = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(pdfUri, "application/pdf")
                        addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }
                    try {
                        ctx.startActivity(open)
                    } catch (e: ActivityNotFoundException) {
                        scope.launch { snackbarHost.showSnackbar("No PDF viewer installed.") }
                    }
                },
                onError = { err ->
                    scope.launch { snackbarHost.showSnackbar("Error: ${err.localizedMessage}") }
                }
            )
        }
        pickedRange = null
    }

    ScreenScaffold("Account Info") { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ui.profile?.let { p ->
                    Text("Name: ${p.displayName}")
                    Text("Email: ${p.email}")
                    Text("Age: ${p.age}")
                    Text("Sex: ${p.sex}")
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { calState.show() },
                    enabled = ui.profile != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate report")
                }

                LaunchedEffect(pickedRange, ui.profile) {
                    if (pickedRange != null && ui.profile != null) {
                        launcher.launch("report_${System.currentTimeMillis()}.pdf")
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHost

            )
        }
    }
}