package com.lumio.app.presentation.screens.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumio.app.crash.CrashHandler

@Composable
fun CrashReportScreen(
    trace: String,
    onContinue: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Last Crash Report", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "LUMIO closed unexpectedly last time. The details below stay on " +
                    "your device only. Please copy this and send it so the bug can " +
                    "be fixed precisely instead of guessed at.",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f)
            ) {
                SelectionContainer {
                    Text(
                        text       = trace,
                        modifier   = Modifier.verticalScroll(rememberScrollState()).padding(12.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 11.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { copyToClipboard(context, trace) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Copy")
                }
                Button(
                    onClick  = { CrashHandler.clearLastCrash(context); onContinue() },
                    modifier = Modifier.weight(1f)
                ) { Text("Continue to App", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("LUMIO crash trace", text))
}
