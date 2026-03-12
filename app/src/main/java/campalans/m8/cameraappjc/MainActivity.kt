package campalans.m8.cameraappjc

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import java.io.File
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            var cameraPermissionGranted by remember { mutableStateOf(false) }
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                cameraPermissionGranted = granted

            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (cameraPermissionGranted) {
                        CameraScreen()
                    } else {
                        Column (
                            verticalArrangement=Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()


                        ) {
                            Text(if (cameraPermissionGranted) "Permission Granted" else "Permission Denied")
                            Button(onClick = {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }) {
                                Text("Demanar permís de càmera")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher per capturar la foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            capturedImageUri = imageUri
        }
    }

    // Funció per crear el fitxer i obtenir l'URI
    fun createImageFile(): Uri {
        val nom = "Cristian_" + java.text.SimpleDateFormat("yyyyMMdd").format(java.util.Date()) + ".mp4"
        val imageFile = File(
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
            nom
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Càmera amb Intent",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = {
                imageUri = createImageFile()
                cameraLauncher.launch(imageUri!!)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obrir Càmera")
        }

        // Mostrar la imatge capturada
        capturedImageUri?.let { uri ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                val player = remember {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(uri))
                        prepare()
                        playWhenReady = true
                    }
                }

                DisposableEffect(player) {
                    onDispose {
                        player.release()
                    }
                }

                AndroidView(
                    factory = {
                        PlayerView(it).apply {
                            this.player = player
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } ?: run {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("La imatge apareixerà aquí")
                    }
                }
            }
        }
    }
}
