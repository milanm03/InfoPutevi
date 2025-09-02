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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.rmas.presentation.singup.SingUpUIEvent
import com.example.rmas.utils.ImageUtils
import com.example.rmas.viewmodels.SingUpViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingUpScreen(navController: NavController, singUpViewModel: SingUpViewModel = viewModel()) {
    val context = LocalContext.current
    val state = singUpViewModel.singUpUIState.collectAsState()
    val scrollState = rememberScrollState()

    val visible = rememberSaveable { mutableStateOf(false) }

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
            singUpViewModel.onEvent(SingUpUIEvent.ImageChanged(it), context, navigateToLogin = {})
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Registracija") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .padding(values)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- Tekstualna polja ostaju ista ---
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    label = { Text(text = "Ime") },
                    value = state.value.ime,
                    onValueChange = { singUpViewModel.onEvent(SingUpUIEvent.ImeChanged(it), context, navigateToLogin = {}) },
                    isError = state.value.imeError != null,
                    supportingText = { if (state.value.imeError != null) Text(text = state.value.imeError!!, color = MaterialTheme.colorScheme.error) }
                )
                // ... (ostala OutlinedTextField polja su ispravna)
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    label = { Text(text = "Prezime") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    value = state.value.prezime,
                    onValueChange = {
                        singUpViewModel.onEvent(
                            SingUpUIEvent.PrezimeChanged(it),
                            context,
                            navigateToLogin = { })
                    },
                    isError = state.value.prezimeError != null,
                    supportingText = {
                        if (state.value.prezimeError != null) {
                            Text(
                                text = state.value.prezimeError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    label = { Text(text = "Telefon") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    value = state.value.telefon,
                    onValueChange = {
                        singUpViewModel.onEvent(
                            SingUpUIEvent.TelefonChanged(it),
                            context,
                            navigateToLogin = { })
                    },
                    isError = state.value.telefonError != null,
                    supportingText = {
                        if (state.value.telefonError != null) {
                            Text(
                                text = state.value.telefonError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    label = { Text(text = "Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    value = state.value.email,
                    onValueChange = {
                        singUpViewModel.onEvent(
                            SingUpUIEvent.EmailChanged(it),
                            context,
                            navigateToLogin = { })
                    },
                    isError = state.value.emailError != null,
                    supportingText = {
                        if (state.value.emailError != null) {
                            Text(
                                text = state.value.emailError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    label = { Text(text = "Korisničko ime") },
                    keyboardOptions = KeyboardOptions.Default,
                    value = state.value.username,
                    onValueChange = {
                        singUpViewModel.onEvent(
                            SingUpUIEvent.UsernameChanged(it),
                            context,
                            navigateToLogin = {})
                    },
                    isError = state.value.usernameError != null,
                    supportingText = {
                        if (state.value.usernameError != null) {
                            Text(
                                text = state.value.usernameError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    label = { Text(text = "Šifra") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    value = state.value.password,
                    isError = state.value.passwordError != null,
                    onValueChange = {
                        singUpViewModel.onEvent(
                            SingUpUIEvent.PasswordChanged(it),
                            context,
                            navigateToLogin = { })
                    },
                    trailingIcon = {
                        val iconImage = if (visible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { visible.value = !visible.value }) {
                            Icon(imageVector = iconImage, contentDescription = "")
                        }
                    },
                    visualTransformation = if (visible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    supportingText = {
                        if (state.value.passwordError != null) {
                            Text(
                                text = state.value.passwordError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                if (state.value.imageError != null) {
                    Text(
                        text = state.value.imageError!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        singUpViewModel.onEvent(
                            SingUpUIEvent.RegisterButtonClicked,
                            context,
                            navigateToLogin = { navController.navigateUp() })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(48.dp),
                    contentPadding = PaddingValues()
                ) {
                    if (singUpViewModel.singUpInProgress.value) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.inversePrimary,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Registruj se",
                            fontSize = 18.sp,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}