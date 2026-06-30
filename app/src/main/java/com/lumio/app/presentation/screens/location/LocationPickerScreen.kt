package com.lumio.app.presentation.screens.location

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.lumio.app.location.GeofenceTrigger

enum class LocationPickMethod { CHOOSE, GPS, MAP }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPickerScreen(
    navController: NavController,
    viewModel: LocationViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar   = remember { SnackbarHostState() }
    var pickMethod by remember { mutableStateOf(LocationPickMethod.CHOOSE) }

    val foregroundPermState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    ) { viewModel.checkPermissions() }

    val backgroundPermState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
            viewModel.checkPermissions()
        }
    } else null

    LaunchedEffect(Unit) { viewModel.checkPermissions() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (pickMethod) {
                            LocationPickMethod.CHOOSE -> "Location Reminder"
                            LocationPickMethod.GPS    -> "Use GPS"
                            LocationPickMethod.MAP    -> "Enter Coordinates"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (pickMethod != LocationPickMethod.CHOOSE) pickMethod = LocationPickMethod.CHOOSE
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost   = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (pickMethod) {
            LocationPickMethod.CHOOSE -> ChooseScreen(
                modifier            = Modifier.padding(padding),
                onGPS               = { pickMethod = LocationPickMethod.GPS },
                onMap               = { pickMethod = LocationPickMethod.MAP },
                hasForeground       = uiState.hasForegroundPermission,
                hasBackground       = uiState.hasBackgroundPermission,
                onRequestForeground = { foregroundPermState.launchMultiplePermissionRequest() },
                onRequestBackground = { backgroundPermState?.launchPermissionRequest() }
            )
            LocationPickMethod.GPS -> GpsScreen(
                modifier  = Modifier.padding(padding),
                uiState   = uiState,
                viewModel = viewModel
            )
            LocationPickMethod.MAP -> MapEntryScreen(
                modifier  = Modifier.padding(padding),
                uiState   = uiState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ChooseScreen(
    modifier: Modifier,
    onGPS: () -> Unit,
    onMap: () -> Unit,
    hasForeground: Boolean,
    hasBackground: Boolean,
    onRequestForeground: () -> Unit,
    onRequestBackground: () -> Unit
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED))))
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("\uD83D\uDCCD", fontSize = 52.sp)
                    Text(
                        "Location Reminder",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                        textAlign  = TextAlign.Center
                    )
                    Text(
                        "Get reminded automatically when you arrive at or leave any place.",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (!hasForeground) {
            item {
                PermissionCard(
                    title   = "Step 1 \u2014 Allow Location",
                    body    = "LUMIO needs to know your location to set up reminders tied to a place.",
                    button  = "Allow Location",
                    onClick = onRequestForeground
                )
            }
        } else if (!hasBackground) {
            item {
                PermissionCard(
                    title   = "Step 2 \u2014 Allow All The Time",
                    body    = "For this reminder to trigger while LUMIO is closed, Android requires " +
                        "\"Allow all the time\" instead of \"While using the app\". " +
                        "This is a system requirement that LUMIO cannot skip.",
                    button  = "Allow All The Time",
                    onClick = onRequestBackground
                )
            }
        }

        item { Text("Choose Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

        item {
            MethodCard(
                emoji    = "\uD83D\uDCE1",
                title    = "Use My GPS Location",
                subtitle = "Detect where you are right now",
                color    = Color(0xFF2563EB),
                features = listOf("No internet needed", "Accurate", "Fast"),
                enabled  = hasForeground && hasBackground,
                onClick  = onGPS
            )
        }

        item {
            MethodCard(
                emoji    = "\uD83D\uDDFA\uFE0F",
                title    = "Enter Coordinates",
                subtitle = "Get coordinates from Google Maps and enter them",
                color    = Color(0xFF7C3AED),
                features = listOf("Any place", "Precise", "Global"),
                enabled  = hasForeground && hasBackground,
                onClick  = onMap
            )
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    body: String,
    button: String,
    onClick: () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            Button(
                onClick = onClick,
                shape   = RoundedCornerShape(10.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(button) }
        }
    }
}

@Composable
private fun MethodCard(
    emoji: String,
    title: String,
    subtitle: String,
    color: Color,
    features: List<String>,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        enabled   = enabled
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 26.sp) }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    features.forEach { tag ->
                        Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.1f)) {
                            Text(
                                tag, fontSize = 10.sp, color = color,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = color)
        }
    }
}

@Composable
private fun GpsScreen(modifier: Modifier, uiState: LocationUiState, viewModel: LocationViewModel) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB).copy(alpha = 0.08f))
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("\uD83D\uDCE1", fontSize = 52.sp)
                    Text("GPS Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Go to the location you want to set, then tap the button below.",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (uiState.currentLocation != null) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF059669).copy(alpha = 0.12f)) {
                            Row(
                                modifier              = Modifier.padding(12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.GpsFixed, null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                                Text(
                                    "Accuracy ~${uiState.currentLocation.accuracy.toInt()}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)))
                    }
                    Button(
                        onClick  = { viewModel.useCurrentLocation() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.MyLocation, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Capture My Location", fontWeight = FontWeight.Bold)
                    }
                    if (uiState.latitude != 0.0) {
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF059669).copy(alpha = 0.1f)) {
                            Row(
                                modifier              = Modifier.padding(12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF059669), modifier = Modifier.size(22.dp))
                                Text("Location captured", fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            }
                        }
                    }
                }
            }
        }
        if (uiState.latitude != 0.0) {
            item { ReminderForm(uiState = uiState, viewModel = viewModel) }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun MapEntryScreen(modifier: Modifier, uiState: LocationUiState, viewModel: LocationViewModel) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How to get coordinates:", fontWeight = FontWeight.Bold)
                    Text("1. Open Google Maps")
                    Text("2. Long press the location you want")
                    Text("3. Coordinates appear at the top, e.g. 9.9312, 76.2673")
                    Text("4. Copy them into the fields below")
                }
            }
        }
        item {
            OutlinedTextField(
                value         = uiState.locationName,
                onValueChange = { viewModel.setLocationName(it) },
                label         = { Text("Location Name *") },
                leadingIcon   = { Icon(Icons.Rounded.LocationOn, null) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(14.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = if (uiState.latitude == 0.0) "" else String.format("%.6f", uiState.latitude),
                    onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setLatitude(v) } },
                    label         = { Text("Latitude") },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(14.dp),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = if (uiState.longitude == 0.0) "" else String.format("%.6f", uiState.longitude),
                    onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setLongitude(v) } },
                    label         = { Text("Longitude") },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(14.dp),
                    singleLine    = true
                )
            }
        }
        item {
            OutlinedButton(
                onClick  = { viewModel.useCurrentLocation() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.MyLocation, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Or Use My Current GPS Location")
            }
        }
        if (uiState.latitude != 0.0) {
            item { ReminderForm(uiState = uiState, viewModel = viewModel) }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ReminderForm(uiState: LocationUiState, viewModel: LocationViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider()
        Text("Reminder Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value          = uiState.title,
            onValueChange  = { viewModel.setTitle(it) },
            label          = { Text("What to remind? *") },
            isError        = uiState.titleError,
            supportingText = { if (uiState.titleError) Text("Please enter a title") },
            leadingIcon    = { Icon(Icons.Rounded.Notifications, null) },
            modifier       = Modifier.fillMaxWidth(),
            shape          = RoundedCornerShape(14.dp)
        )
        OutlinedTextField(
            value         = uiState.description,
            onValueChange = { viewModel.setDescription(it) },
            label         = { Text("Details (optional)") },
            leadingIcon   = { Icon(Icons.Rounded.Description, null) },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(14.dp),
            minLines      = 2
        )
        Text("When to remind?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GeofenceTrigger.values().forEach { trigger ->
                val selected = uiState.triggerType == trigger
                FilterChip(
                    selected = selected,
                    onClick  = { viewModel.setTrigger(trigger) },
                    label    = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(trigger.emoji, fontSize = 18.sp)
                            Text(trigger.label, fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    },
                    modifier = Modifier.weight(1f).height(68.dp),
                    shape    = RoundedCornerShape(12.dp)
                )
            }
        }
        Card(
            shape  = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Alert Radius", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            "${uiState.radiusMeters.toInt()} m",
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize   = 12.sp,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Slider(value = uiState.radiusMeters, onValueChange = { viewModel.setRadius(it) }, valueRange = 50f..1000f, steps = 18)
            }
        }
        Button(
            onClick  = { viewModel.saveLocationReminder() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            enabled  = !uiState.isSaving && uiState.latitude != 0.0
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Location Reminder", fontWeight = FontWeight.Bold)
            }
        }
    }
}
