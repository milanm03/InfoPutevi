package com.example.rmas.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rmas.data.Location
import com.example.rmas.database.Firebase
import com.example.rmas.viewmodels.LoginViewModel
import com.example.rmas.services.location.UserLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter
import com.example.rmas.components.BottomSheet
import com.example.rmas.viewmodels.FilterViewModel
import com.google.firebase.Timestamp
import java.util.Date
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Man
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.rmas.components.LocationBottomSheet
import com.example.rmas.services.location.LocationService
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.MarkerComposable

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    startDestination: String,
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(),
    filterViewModel: FilterViewModel = viewModel()
) {
    val sheetState = rememberModalBottomSheetState()
    val isSheetOpen = rememberSaveable { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState()
    val isBottomSheetOpen = rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val state = filterViewModel.filterUIState.collectAsState()

    var selectedIndex by rememberSaveable {
        mutableIntStateOf(1)
    }

    val userLocation by remember { mutableStateOf(UserLocation.location) }

    var locations by remember { mutableStateOf(emptyList<Location>()) }
    var locationsCopy by remember { mutableStateOf(emptyList<Location>()) }
    Firebase.getLocations {
        locations = it
        locationsCopy = locations
    }

    var ime by remember {
        mutableStateOf("")
    }
    var prezime by remember {
        mutableStateOf("")
    }
    var email by remember {
        mutableStateOf("")
    }
    var img by remember {
        mutableStateOf("")
    }
    if (FirebaseAuth.getInstance().currentUser != null) {
        Firebase.getUser(FirebaseAuth.getInstance().currentUser!!.uid) {
            if (it != null) {
                ime = it.ime
                prezime = it.prezime
                email = it.email
                img = it.image
            }
        }
    }

    val deviceLatLng by remember {
        mutableStateOf(LatLng(43.32, 21.89))
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(deviceLatLng, 15f)
    }
    val uiSettings by remember {
        mutableStateOf(MapUiSettings(zoomControlsEnabled = true, compassEnabled = true))
    }
    val properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.TERRAIN))
    }
    var navLabel by rememberSaveable {
        mutableStateOf("Mapa")
    }

    val isPickerVisible = remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    val sliderPosition = rememberSaveable { mutableFloatStateOf(0f) }
    val showSecondSheet = rememberSaveable { mutableStateOf(false) }
    val clickedLocation = rememberSaveable { mutableStateOf("") }

    if (userLocation.value != null) {
        locations = locationsCopy
        if (state.value.types.isNotEmpty()) {
            locations = locations.filter {
                it.type in state.value.types
            }
        }
        if (state.value.users.isNotEmpty()) {
            locations = locations.filter {
                it.userId in state.value.users
            }
        }
        if (state.value.distance != null) {
            locations = locations.filter {
                val startLatLng = LatLng(it.latitude, it.longitude)
                val endLatLng =
                    LatLng(userLocation.value!!.latitude, userLocation.value!!.longitude)
                SphericalUtil.computeDistanceBetween(
                    startLatLng,
                    endLatLng
                ) <= state.value.distance!! * 1000
            }
        }
        if (state.value.startDate != null && state.value.endDate != null) {
            locations = locations.filter {
                it.date >= Timestamp(Date(state.value.startDate!!)) && it.date <= Timestamp(
                    Date(
                        state.value.endDate!!
                    )
                )
            }
        }
        if (state.value.searchText != "") {
            locations = locations.filter {
                it.doesMatchSearchQuery(state.value.searchText)
            }
        }
    }


    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val isTrackingServiceEnabled by rememberSaveable {
        mutableStateOf(
            sharedPreferences.getBoolean(
                "location_tracking",
                false
            )
        )
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
    } else {
        if (isTrackingServiceEnabled) {
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_START_NEARBY
                context.startForegroundService(this)
            }
        } else {
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                context.startForegroundService(this)
            }
        }

    }
    var checked by rememberSaveable { mutableStateOf(isTrackingServiceEnabled) }

    if (userLocation.value != null) {
        LaunchedEffect(key1 = true) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        LatLng(
                            userLocation.value!!.latitude,
                            userLocation.value!!.longitude
                        ), 25f, 0f, 0f
                    )
                ),
                durationMs = 1000
            )
        }
    }
    ModalNavigationDrawer(
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet {
                LazyColumn(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = img),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(70.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$ime $prezime",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = email, style = MaterialTheme.typography.bodySmall
                            )
                        }
                        NavigationDrawerItem(
                            label = { Text(text = "Mapa") },
                            selected = selectedIndex == 1,
                            onClick = {
                                selectedIndex = 1
                                navLabel = "Mapa"
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Map,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text(text = "Rang lista") },
                            selected = selectedIndex == 2,
                            onClick = {
                                selectedIndex = 2
                                navLabel = "Rang lista"
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Equalizer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        NavigationDrawerItem(
                            label = { Text(text = "Lista lokacija") },
                            selected = selectedIndex == 3,
                            onClick = {
                                selectedIndex = 3
                                navLabel = "Lista lokacija"
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                        HorizontalDivider(modifier = Modifier.padding(5.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(NavigationDrawerItemDefaults.ItemPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(start = 17.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddAlert,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(text = "Notifikacije", fontSize = 14.sp)
                            }
                            Switch(checked = checked,
                                onCheckedChange = {
                                    if (ActivityCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        Toast.makeText(
                                            context,
                                            "Potrebno je da uključite lokaciju kako biste mogli da aktivirate notifikacije.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        checked = it
                                        if (it) {
                                            Intent(context, LocationService::class.java).apply {
                                                action = LocationService.ACTION_START_NEARBY
                                                context.startForegroundService(this)
                                            }
                                            with(sharedPreferences.edit()) {
                                                putBoolean("location_tracking", true)
                                                apply()
                                            }
                                        } else {
                                            Intent(context, LocationService::class.java).apply {
                                                action = LocationService.ACTION_STOP
                                                context.stopService(this)
                                            }
                                            Intent(context, LocationService::class.java).apply {
                                                action = LocationService.ACTION_START
                                                context.startForegroundService(this)
                                            }
                                            with(sharedPreferences.edit()) {
                                                putBoolean("location_tracking", false)
                                                apply()
                                            }
                                        }
                                    }
                                },
                                thumbContent = if (checked) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                    item {
                        NavigationDrawerItem(
                            label = { Text(text = "Odjavi se") },
                            selected = false,
                            onClick = {
                                loginViewModel.logOut(context, navigateToLogin = {
                                    if (startDestination == "HomeScreen") {
                                        navController.navigate("LoginScreen") {
                                            popUpTo("HomeScreen") { inclusive = true }
                                        }
                                    } else navController.navigateUp()
                                })
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }, drawerState = drawerState
    ) {
        Scaffold(topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = navLabel) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                        )
                    }
                },
                actions = {
                    if (selectedIndex == 1) {
                        IconButton(onClick = {
                            isSheetOpen.value = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter",
                            )
                        }
                    } else null
                })
        }, floatingActionButton = {
            if (selectedIndex == 1) {
                FloatingActionButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = { navController.navigate("AddMarkerScreen") },
                ) {
                    Icon(
                        Icons.Filled.AddLocationAlt, contentDescription = ""
                    )
                }
            } else null
        },
            floatingActionButtonPosition = FabPosition.Start
        ) { values ->
            Surface(
                color = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(values)
            ) {
                when (selectedIndex) {
                    1 -> {
                        Box(modifier = Modifier) {
                            GoogleMap(modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = properties,
                                uiSettings = uiSettings,
                                onMapClick = {}) {
                                if (userLocation.value != null) {
                                    MarkerComposable(
                                        title = "Vaša lokacija",
                                        state = MarkerState(
                                            position = LatLng(
                                                userLocation.value!!.latitude,
                                                userLocation.value!!.longitude
                                            )
                                        )
                                    ) {
                                        Icon(
                                            modifier = Modifier
                                                .size(55.dp),
                                            imageVector = Icons.Filled.Man, contentDescription = "",
                                            tint = Color.Black
                                        )
                                    }
                                }
                                locations.let { locations ->
                                    for (marker in locations)
                                        Marker(
                                            onClick = {
                                                isBottomSheetOpen.value = true
                                                clickedLocation.value = marker.id
                                                false
                                            },
                                            state = MarkerState(
                                                position = LatLng(
                                                    marker.latitude,
                                                    marker.longitude
                                                )
                                            )
                                        )
                                }
                            }
                        }
                    }

                    2 -> {
                        LeaderboardScreen()
                    }

                    else -> {
                        LocationScreen()
                    }
                }
                if (isSheetOpen.value) {
                    BottomSheet(
                        isSheetOpen = isSheetOpen,
                        sheetState = sheetState,
                        isPickerVisible = isPickerVisible,
                        dateRangePickerState = dateRangePickerState,
                        sliderPosition = sliderPosition,
                        showSecondSheet = showSecondSheet,
                        filterViewModel = filterViewModel,
                        state = state
                    )
                }
                if (isBottomSheetOpen.value) {
                    LocationBottomSheet(
                        sheetState = bottomSheetState,
                        isSheetOpen = isBottomSheetOpen,
                        locationId = clickedLocation.value
                    )
                }
            }
        }
    }
}


