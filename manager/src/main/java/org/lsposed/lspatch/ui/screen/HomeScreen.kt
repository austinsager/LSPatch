package org.lsposed.lspatch.ui.screen
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lsposed.lspatch.ui.viewmodel.PatchViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: PatchViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val ctx = LocalContext.current
    val listState = rememberLazyListState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { vm.onApkSelected(it, it.lastPathSegment?.substringAfterLast('/') ?: "app.apk") }
    }
    LaunchedEffect(state.logs.size) { if (state.logs.isNotEmpty()) listState.animateScrollToItem(state.logs.lastIndex) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("LSPatch 2.3.0 Ultimate — Erik") }, actions = { IconButton(onClick = vm::clearLogs) { Icon(Icons.Default.Delete, null) } }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (!state.isPatching) vm.startPatch(ctx) },
                icon = { Icon(if (state.isPatching) Icons.Default.HourglassBottom else Icons.Default.Build, null) },
                text = { Text(if (state.isPatching) "Patching…" else "PATCH APK") }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Target APK", style = MaterialTheme.typography.titleMedium)
                    Text(state.selectedApkName)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { picker.launch(arrayOf("application/vnd.android.package-archive", "*/*")) }) {
                        Icon(Icons.Default.FolderOpen, null); Spacer(Modifier.width(8.dp)); Text("Select APK")
                    }
                }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.lastOutput?.let { Text("Output: $it", color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(16.dp))
            Text("Live Log", style = MaterialTheme.typography.titleMedium)
            Card(Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(state = listState, modifier = Modifier.padding(8.dp)) {
                    items(state.logs) { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}
