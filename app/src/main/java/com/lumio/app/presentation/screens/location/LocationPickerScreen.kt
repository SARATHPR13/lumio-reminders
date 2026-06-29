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

    val permList = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }
    val permState = rememberMultiplePermissionsState(permList) { results ->
        if (results.values.all { it }) viewModel.checkPermissions()
    }

    LaunchedEffect(Unit) {
        if (!permState.allPermissionsGranted) permState.launchMultiplePermissionRequest()
        else viewModel.checkPermissions()
    }
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
                        if (pickMethod != LocationPickMethod.CHOOSE)
                            pickMethod = LocationPickMethod.CHOOSE
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
            LocationPickMethod.CHOOSE ->
                ChooseScreen(
                    modifier            = Modifier.padding(padding),
                    onGPS               = { pickMethod = LocationPickMethod.GPS },
                    onMap               = { pickMethod = LocationPickMethod.MAP },
                    hasPermission       = permState.allPermissionsGranted,
                    onRequestPermission = { permState.launchMultiplePermissionRequest() }
                )
            LocationPickMethod.GPS ->
                GpsScreen(
                    modifier  = Modifier.padding(padding),
                    uiState   = uiState,
                    viewModel = viewModel
                )
            LocationPickMethod.MAP ->
                MapScreen(
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
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
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
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A73E8), Color(0xFF7B2FBE))
                        )
                    )
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("\uD83D\uDCCD", fontSize = 52.sp)
                    Text(
                        "Location Reminder",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                        textAlign  = TextAlign.Center
                    )
                    Text(
                        "Get notified when you arrive or leave any place!",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (!hasPermission) {
            item {
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier              = Modifier.padding(14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.Warning, null,
                            tint     = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(22.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Location Permission Needed",
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Required for location reminders",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Button(
                            onClick = onRequestPermission,
                            shape   = RoundedCornerShape(8.dp),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("Allow") }
                    }
                }
            }
        }

        item {
            Text("Choose How to Set Location",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        }

        item {
            OptionCard(
                emoji    = "\uD83D\uDCE1",
                title    = "Use My GPS",
                subtitle = "Detect your current location automatically",
                color    = Color(0xFF1A73E8),
                features = listOf("No internet needed", "Very accurate", "One tap — instant"),
                btnText  = "Use GPS Location",
                enabled  = hasPermission,
                onClick  = onGPS
            )
        }

        item {
            OptionCard(
                emoji    = "\uD83D\uDDFA\uFE0F",
                title    = "Enter Coordinates",
                subtitle = "Get coordinates from Google Maps and enter them",
                color    = Color(0xFF7B2FBE),
                features = listOf("Pick any place in the world", "No GPS needed", "Works anywhere"),
                btnText  = "Enter Location",
                enabled  = true,
                onClick  = onMap
            )
        }

        item {
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("How It Works", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall)
                    Text("1. Set a location (office, home, shop)")
                    Text("2. Choose: remind when you Arrive or Leave")
                    Text("3. LUMIO monitors your location silently")
                    Text("4. Notification fires when triggered!")
                    Text("Very low battery usage",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun OptionCard(
    emoji: String, title: String, subtitle: String,
    color: Color, features: List<String>,
    btnText: String, enabled: Boolean, onClick: () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(color.copy(alpha = 0.13f))
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(emoji, fontSize = 36.sp)
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold, color = color)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                features.forEach { f -> Text("✅ $f", style = MaterialTheme.typography.bodySmall) }
                Button(
                    onClick  = onClick,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape    = RoundedCornerShape(10.dp),
                    enabled  = enabled,
                    colors   = ButtonDefaults.buttonColors(containerColor = color)
                ) { Text(btnText, fontWeight = FontWeight.Bold) }
                if (!enabled) {
                    Text("Grant location permission first",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun GpsScreen(
    modifier: Modifier, uiState: LocationUiState, viewModel: LocationViewModel
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A73E8).copy(alpha = 0.08f)
                )
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("\uD83D\uDCE1", fontSize = 52.sp)
                    Text("GPS Location", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text(
                        "Go to the place you want to set then tap the button below",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (uiState.currentLocation != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier              = Modifier.padding(12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.GpsFixed, null,
                                    tint     = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp))
                                Column {
                                    Text("GPS Signal: Good",
                                        fontWeight = FontWeight.SemiBold,
                                        color      = Color(0xFF4CAF50), fontSize = 13.sp)
                                    Text(
                                        "Accuracy: ~\${uiState.currentLocation.accuracy.toInt()}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text("Getting GPS signal...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick  = { viewModel.useCurrentLocation() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                    ) {
                        Icon(Icons.Rounded.MyLocation, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Use This Location", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    if (uiState.latitude != 0.0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier              = Modifier.padding(12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, null,
                                    tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                Text("Location Captured!", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        item { ReminderForm(uiState = uiState, viewModel = viewModel) }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun MapScreen(
    modifier: Modifier, uiState: LocationUiState, viewModel: LocationViewModel
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("How to find coordinates:", fontWeight = FontWeight.Bold)
                    Text("1. Open Google Maps app")
                    Text("2. Long press on the location")
                    Text("3. Coordinates appear at top (e.g. 9.9312, 76.2673)")
                    Text("4. Copy and paste them below")
                }
            }
        }
        item {
            OutlinedTextField(
                value         = uiState.locationName,
                onValueChange = { viewModel.setLocationName(it) },
                label         = { Text("Location Name *") },
                placeholder   = { Text("e.g. My Office, Lulu Mall") },
                leadingIcon   = { Icon(Icons.Rounded.LocationOn, null) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = if (uiState.latitude == 0.0) "" else String.format("%.6f", uiState.latitude),
                    onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setLatitude(v) } },
                    label         = { Text("Latitude") },
                    placeholder   = { Text("e.g. 9.9312") },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(12.dp),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = if (uiState.longitude == 0.0) "" else String.format("%.6f", uiState.longitude),
                    onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.setLongitude(v) } },
                    label         = { Text("Longitude") },
                    placeholder   = { Text("e.g. 76.2673") },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(12.dp),
                    singleLine    = true
                )
            }
        }
        item {
            OutlinedButton(
                onClick  = { viewModel.useCurrentLocation() },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.MyLocation, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Or Use My Current GPS")
            }
        }
        if (uiState.latitude != 0.0) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier              = Modifier.padding(12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null,
                            tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                        Text("Location Set: \${uiState.locationName}",
                            fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
        item { ReminderForm(uiState = uiState, viewModel = viewModel) }
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
            placeholder    = { Text("e.g. Call Manager") },
            isError        = uiState.titleError,
            supportingText = { if (uiState.titleError) Text("Please enter a title") },
            leadingIcon    = { Icon(Icons.Rounded.Notifications, null) },
            modifier       = Modifier.fillMaxWidth(),
            shape          = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value         = uiState.description,
            onValueChange = { viewModel.setDescription(it) },
            label         = { Text("Details (optional)") },
            leadingIcon   = { Icon(Icons.Rounded.Description, null) },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(trigger.emoji, fontSize = 20.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(trigger.label, fontSize = 10.sp, textAlign = TextAlign.Center)
                        }
                    },
                    modifier = Modifier.weight(1f).height(70.dp),
                    shape    = RoundedCornerShape(12.dp)
                )
            }
        }
        Card(
            shape  = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Alert Radius", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("\${uiState.radiusMeters.toInt()} m",
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Bold, fontSize = 12.sp,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Slider(value = uiState.radiusMeters, onValueChange = { viewModel.setRadius(it) },
                    valueRange = 50f..1000f, steps = 18)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("50m precise", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("1000m wide", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Button(
            onClick  = { viewModel.saveLocationReminder() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            enabled  = !uiState.isSaving && uiState.latitude != 0.0
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Location Reminder", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
