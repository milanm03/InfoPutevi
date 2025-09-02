package com.example.rmas.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.rmas.presentation.marker.MarkerUIEvent
import com.example.rmas.utils.ImageUtils
import com.example.rmas.viewmodels.MarkerViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarkerScreen(navController: NavController, markerViewModel: MarkerViewModel = viewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val state = markerViewModel.markerUIState.collectAsState()

    val imageUtils = remember { ImageUtils(context) }

    // State za URI slike, sa Saver-om da preživi promenu konfiguracije.
    val uriSaver = Saver<Uri?, String>(
        save = { it?.toString() ?: "" },
        restore = { if (it.isNotEmpty()) Uri.parse(it) else null }
    )
    var imageUri by rememberSaveable(stateSaver = uriSaver) {
        mutableStateOf<Uri?>(null)
    }

    // Launcher za dobijanje rezultata od kamere ili galerije.
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Uri? = result.data?.data
                val isCamera = data == null

                imageUri = if (isCamera) {
                    // Slučaj KAMERE: Konvertujemo sačuvanu putanju (String) u Uri
                    imageUtils.currentPhotoPath?.let { path ->
                        File(path).toUri()
                    }
                } else {
                    // Slučaj GALERIJE: Koristimo dobijeni Uri direktno!
                    data
                }
            }
        }

    // Launcher za traženje dozvole za kameru.
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launcher.launch(imageUtils.getIntent())
        }
    }

    // Poziva se svaki put kada se imageUri promeni, da bi se ažurirao ViewModel.
    LaunchedEffect(imageUri) {
        imageUri?.let {
            markerViewModel.onEvent(MarkerUIEvent.ImageChanged(it), context, onClick = {})
        }
    }

    val options =
        listOf("Rupa na putu", "Radovi na putu", "Saobraćajna nezgoda", "Zatvorena ulica", "Semafor", "Restoran", "Odmoriste", "Ostalo")
    var expanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Dodaj na mapu") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        }
    ) { values ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(values) // Koristi padding iz Scaffold-a
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp) // Dodajemo padding sa strane
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(28.dp)) // Razmak od TopAppBar-a

                // --- ISPRAVLJENI DEO ZA PRIKAZ SLIKE ---
                val imageModifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            launcher.launch(imageUtils.getIntent())
                        }
                    }

                if (imageUri == null) {
                    Box(
                        modifier = imageModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Dodaj sliku",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUri),
                        contentDescription = "Izabrana slika",
                        contentScale = ContentScale.Crop,
                        modifier = imageModifier
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                if (state.value.imageError != null) {
                    Text(
                        text = state.value.imageError!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 12.dp),
                        fontSize = 12.sp
                    )
                }

                // --- Ostatak forme (OutlinedTextFields, Dropdown, Button) ---
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    value = state.value.title,
                    onValueChange = {
                        markerViewModel.onEvent(
                            MarkerUIEvent.TitleChanged(it),
                            context,
                            onClick = {})
                    },
                    label = { Text(text = "Naslov") },
                    keyboardOptions = KeyboardOptions.Default,
                    isError = state.value.titleError != null,
                    supportingText = {
                        if (state.value.titleError != null) {
                            Text(
                                text = state.value.titleError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    value = state.value.description,
                    onValueChange = {
                        markerViewModel.onEvent(
                            MarkerUIEvent.DescriptionChanged(it),
                            context,
                            onClick = {})
                    },
                    label = { Text(text = "Opis") },
                    keyboardOptions = KeyboardOptions.Default,
                    isError = state.value.descriptionError != null,
                    supportingText = {
                        if (state.value.descriptionError != null) {
                            Text(
                                text = state.value.descriptionError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .menuAnchor(),
                        value = state.value.type,
                        onValueChange = {},
                        label = { Text(text = "Kategorija") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        readOnly = true
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach {
                            DropdownMenuItem(
                                text = { Text(text = it) },
                                onClick = {
                                    expanded = false
                                    markerViewModel.onEvent(
                                        MarkerUIEvent.TypeChanged(it),
                                        context,
                                        onClick = {})
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        markerViewModel.onEvent(
                            MarkerUIEvent.AddMarkerClicked,
                            context,
                            onClick = { navController.navigateUp() })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(48.dp),
                    contentPadding = PaddingValues(),
                ) {
                    if (markerViewModel.addInProgress.value) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.inversePrimary,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Dodaj na mapu",
                            fontSize = 18.sp,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp)) // Razmak na dnu
            }
        }
    }
}