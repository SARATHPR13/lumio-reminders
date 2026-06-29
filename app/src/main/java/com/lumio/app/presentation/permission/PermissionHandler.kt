package com.lumio.app.presentation.permission

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MicrophonePermissionHandler(
    onGranted: @Composable () -> Unit,
    onDenied: @Composable () -> Unit = {}
) {
    val micPermState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var showRationale by remember { mutableStateOf(false) }

    when {
        micPermState.status.isGranted -> {
            onGranted()
        }
        micPermState.status.shouldShowRationale || !micPermState.status.isGranted -> {
            if (showRationale) {
                MicrophoneRationaleDialog(
                    onAllow   = {
                        showRationale = false
                        micPermState.launchPermissionRequest()
                    },
                    onDismiss = { showRationale = false }
                )
            }
            LaunchedEffect(Unit) { showRationale = true }
            onDenied()
        }
    }
}

@Composable
fun MicrophoneRationaleDialog(
    onAllow: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Rounded.Mic,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                "Microphone Permission",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "LUMIO needs microphone access to let you create reminders using your voice.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier            = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PermissionFeatureRow("Voice reminders — just speak!")
                        PermissionFeatureRow("Hands-free reminder creation")
                        PermissionFeatureRow("Works offline — no cloud needed")
                    }
                }
                Text(
                    "Your voice is never recorded or stored. It is only used while you are speaking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAllow,
                shape   = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Rounded.Mic, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Allow Microphone", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape   = RoundedCornerShape(10.dp)
            ) { Text("Not Now") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun PermissionFeatureRow(text: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Rounded.CheckCircle, null,
            tint     = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionHandler(content: @Composable () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notifPerm = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        var showDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (!notifPerm.status.isGranted) showDialog = true
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                icon = {
                    Icon(Icons.Rounded.NotificationsActive, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp))
                },
                title = { Text("Enable Notifications", fontWeight = FontWeight.Bold) },
                text  = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("LUMIO needs notification permission to send you reminders at the right time.")
                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier            = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PermissionFeatureRow("Get reminded at scheduled times")
                                PermissionFeatureRow("Snooze or mark done from notification")
                                PermissionFeatureRow("Location-based alerts")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            notifPerm.launchPermissionRequest()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Enable Notifications", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Later") }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
    content()
}
