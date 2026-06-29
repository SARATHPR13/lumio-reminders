package com.lumio.app.presentation.screens.location

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
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
                            LocationPickMethod.GPS    -> "Use My Location"
                            LocationPickMethod.MAP    -> "Drop Pin on Map"
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (pickMethod) {
                LocationPickMethod.CHOOSE ->
                    ChooseScreen(
                        onGPS               = { pickMethod = LocationPickMethod.GPS },
                        onMap               = { pickMethod = LocationPickMethod.MAP },
                        hasPermission       = permState.allPermissionsGranted,
                        onRequestPermission = { permState.launchMultiplePermissionRequest() }
                    )
                LocationPickMethod.GPS ->
                    GpsScreen(uiState = uiState, viewModel = viewModel)
                LocationPickMethod.MAP ->
                    MapScreen(
                        uiState       = uiState,
                        viewModel     = viewModel,
                        hasPermission = permState.allPermissionsGranted
                    )
            }
        }
    }
}

@Composable
private fun ChooseScreen(
    onGPS: () -> Unit,
    onMap: () -> Unit,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    LazyColumn(
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("📍", fontSize = 48.sp)
                    Text("Location Reminder",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                        textAlign  = TextAlign.Center)
                    Text("Automatically get reminded when you arrive at or leave any place",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center)
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
                        modifier              = Modifier.padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.LocationOff, null,
                            tint     = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Location Permission Required",
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Needed to monitor your location",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Button(
                            onClick = onRequestPermission,
                            colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape   = RoundedCornerShape(10.dp)
                        ) { Text("Allow") }
                    }
                }
            }
        }

        item {
            Text("How would you like to set the location?",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        }

        // GPS Option
        item {
            Card(
                onClick   = onGPS,
                shape     = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors    = CardDefaults.cardColors(containerColor = Color(0xFF2563EB).copy(alpha = 0.08f))
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.MyLocation, null,
                            tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp)) }
                    Column(Modifier.weight(1f)) {
                        Text("Use My Current Location",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Instantly captures where you are right now using GPS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("No internet", "Very accurate", "Instant").forEach { tag ->
                                Surface(shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF2563EB).copy(alpha = 0.1f)) {
                                    Text(tag, fontSize = 10.sp, color = Color(0xFF2563EB),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Icon(Icons.Rounded.ChevronRight, null,
                        tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                }
            }
        }

        // Map Option
        item {
            Card(
                onClick   = onMap,
                shape     = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors    = CardDefaults.cardColors(containerColor = Color(0xFF7C3AED).copy(alpha = 0.08f))
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.Map, null,
                            tint = Color(0xFF7C3AED), modifier = Modifier.size(28.dp)) }
                    Column(Modifier.weight(1f)) {
                        Text("Drop Pin on Google Map",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Tap anywhere on the map to set any location in the world",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Visual map", "Any place", "Search").forEach { tag ->
                                Surface(shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF7C3AED).copy(alpha = 0.1f)) {
                                    Text(tag, fontSize = 10.sp, color = Color(0xFF7C3AED),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Icon(Icons.Rounded.ChevronRight, null,
                        tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun MapScreen(
    uiState: LocationUiState,
    viewModel: LocationViewModel,
    hasPermission: Boolean
) {
    val defaultLatLng   = LatLng(20.5937, 78.9629) // Center of India
    var markerPosition  by remember { mutableStateOf(
        if (uiState.latitude != 0.0 && uiState.longitude != 0.0)
            LatLng(uiState.latitude, uiState.longitude)
        else if (uiState.currentLocation != null)
            LatLng(uiState.currentLocation.latitude, uiState.currentLocation.longitude)
        else defaultLatLng
    )}
    var showSaveSheet   by remember { mutableStateOf(false) }
    var searchText      by remember { mutableStateOf("") }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 13f)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Search bar
        Card(
            modifier  = Modifier.fillMaxWidth().padding(12.dp),
            shape     = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier          = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = searchText,
                    onValueChange = { searchText = it },
                    placeholder   = { Text("Search location or address...") },
                    leadingIcon   = { Icon(Icons.Rounded.Search, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }

        // Map
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier            = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties          = MapProperties(
                    isMyLocationEnabled = hasPermission,
                    mapType             = MapType.NORMAL
                ),
                uiSettings          = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled     = false,
                    compassEnabled          = true,
                    mapToolbarEnabled       = false
                ),
                onMapClick = { latLng ->
                    markerPosition = latLng
                    viewModel.setLatitude(latLng.latitude)
                    viewModel.setLongitude(latLng.longitude)
                    viewModel.setLocationName(
                        "Lat: ${String.format("%.4f", latLng.latitude)}, " +
                        "Lng: ${String.format("%.4f", latLng.longitude)}"
                    )
                    showSaveSheet = true
                }
            ) {
                if (uiState.latitude != 0.0 || uiState.longitude != 0.0) {
                    Marker(
                        state     = MarkerState(position = markerPosition),
                        title     = uiState.locationName.ifBlank { "Selected Location" },
                        snippet   = "Tap map to move pin",
                        draggable = true
                    )
                }
            }

            // Instruction overlay
            if (uiState.latitude == 0.0) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    )
                ) {
                    Row(
                        modifier              = Modifier.padding(12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("👆", fontSize = 18.sp)
                        Text("Tap anywhere on the map to drop a pin",
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.inverseOnSurface)
                    }
                }
            }

            // My Location FAB
            FloatingActionButton(
                onClick        = {
                    viewModel.useCurrentLocation()
                    uiState.currentLocation?.let { loc ->
                        markerPosition = LatLng(loc.latitude, loc.longitude)
                    }
                },
                modifier       = Modifier.align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = if (showSaveSheet) 240.dp else 80.dp)
                    .size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor   = MaterialTheme.colorScheme.primary,
                shape          = CircleShape,
                elevation      = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Rounded.MyLocation, null, modifier = Modifier.size(22.dp))
            }

            // Pin dropped — show save panel
            if (showSaveSheet && uiState.latitude != 0.0) {
                Card(
                    modifier  = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    shape     = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    elevation = CardDefaults.cardElevation(16.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier            = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.LocationOn, null,
                                tint     = Color(0xFF2563EB),
                                modifier = Modifier.size(24.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Pin Dropped!", fontWeight = FontWeight.Bold)
                                Text(
                                    "Lat: ${String.format("%.5f", uiState.latitude)}, " +
                                    "Lng: ${String.format("%.5f", uiState.longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedTextField(
                            value         = uiState.locationName,
                            onValueChange = { viewModel.setLocationName(it) },
                            label         = { Text("Name this location") },
                            placeholder   = { Text("e.g. My Office, Lulu Mall") },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            singleLine    = true,
                            leadingIcon   = { Icon(Icons.Rounded.Label, null) }
                        )

                        OutlinedTextField(
                            value         = uiState.title,
                            onValueChange = { viewModel.setTitle(it) },
                            label         = { Text("Remind me to...") },
                            placeholder   = { Text("e.g. Call Manager") },
                            isError       = uiState.titleError,
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            singleLine    = true,
                            leadingIcon   = { Icon(Icons.Rounded.Notifications, null) }
                        )

                        // Trigger
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GeofenceTrigger.values().forEach { trigger ->
                                val sel = uiState.triggerType == trigger
                                FilterChip(
                                    selected = sel,
                                    onClick  = { viewModel.setTrigger(trigger) },
                                    label    = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 2.dp)) {
                                            Text(trigger.emoji, fontSize = 16.sp)
                                            Text(trigger.label, fontSize = 10.sp)
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(60.dp),
                                    shape    = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Button(
                            onClick  = { viewModel.saveLocationReminder() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            enabled  = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(20.dp),
                                    color       = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Save Location Reminder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GpsScreen(uiState: LocationUiState, viewModel: LocationViewModel) {
    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2563EB).copy(alpha = 0.08f)
                )
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("📡", fontSize = 52.sp)
                    Text("GPS Location", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text("Walk to the location you want to set, then tap the button below",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)

                    if (uiState.currentLocation != null) {
                        Surface(shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF059669).copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.GpsFixed, null,
                                    tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                                Column {
                                    Text("GPS Signal Strong",
                                        fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                        color = Color(0xFF059669))
                                    Text("Accuracy: ~${uiState.currentLocation.accuracy.toInt()}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
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
                        Text("Capture My Location", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    if (uiState.latitude != 0.0) {
                        Surface(shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF059669).copy(alpha = 0.08f)) {
                            Row(modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Rounded.CheckCircle, null,
                                    tint = Color(0xFF059669), modifier = Modifier.size(22.dp))
                                Column {
                                    Text("Location Captured!", fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669))
                                    Text("${String.format("%.5f", uiState.latitude)}, " +
                                        "${String.format("%.5f", uiState.longitude)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.latitude != 0.0) {
            item {
                OutlinedTextField(value = uiState.locationName,
                    onValueChange = { viewModel.setLocationName(it) },
                    label = { Text("Location Name") },
                    placeholder = { Text("e.g. My Office, Home") },
                    leadingIcon = { Icon(Icons.Rounded.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
            }
            item {
                OutlinedTextField(value = uiState.title,
                    onValueChange = { viewModel.setTitle(it) },
                    label = { Text("What to remind?") },
                    placeholder = { Text("e.g. Call Manager") },
                    isError = uiState.titleError,
                    leadingIcon = { Icon(Icons.Rounded.Notifications, null) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GeofenceTrigger.values().forEach { trigger ->
                        val sel = uiState.triggerType == trigger
                        FilterChip(selected = sel, onClick = { viewModel.setTrigger(trigger) },
                            label = { Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(trigger.emoji, fontSize = 16.sp)
                                Text(trigger.label, fontSize = 10.sp) } },
                            modifier = Modifier.weight(1f).height(60.dp),
                            shape = RoundedCornerShape(12.dp))
                    }
                }
            }
            item {
                Button(onClick = { viewModel.saveLocationReminder() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp), enabled = !uiState.isSaving) {
                    if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else { Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Save Reminder", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
