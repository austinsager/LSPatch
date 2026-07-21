package org.lsposed.lspatch.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.lsposed.lspatch.ui.viewmodel.PatchViewModel

@Composable
fun ManagerScreen(viewModel: PatchViewModel) {
    val context = LocalContext.current
    var targetApkUri by remember { mutableStateOf<Uri?>(null) }
    var moduleUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // SAF Picker for Target APK
    val apkPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { targetApkUri = it }
    }

    // SAF Picker for Xposed Modules
    val modulePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        moduleUris = uris
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "LSPatch Manager", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Target APK: ${targetApkUri?.lastPathSegment ?: "None selected"}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { apkPicker.launch(arrayOf("application/vnd.android.package-archive")) },
                    enabled = !viewModel.isProcessing
                ) {
                    Text("Select Target APK")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Modules Selected: ${moduleUris.size}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { modulePicker.launch(arrayOf("application/vnd.android.package-archive")) },
                    enabled = !viewModel.isProcessing
                ) {
                    Text("Select Xposed Modules")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (viewModel.isProcessing) {
            CircularProgressIndicator()
        }
        
        Text(text = viewModel.patchStatus, style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = {
                targetApkUri?.let { uri ->
                    viewModel.executeLocalPatch(context, uri, moduleUris)
                }
            },
            enabled = targetApkUri != null && !viewModel.isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Patching")
        }
    }
}
