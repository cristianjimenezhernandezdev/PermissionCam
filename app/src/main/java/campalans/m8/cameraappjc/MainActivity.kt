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

/*S’han fet dos canvis principals al projecte perquè l’aplicació pugui funcionar correctament:
la gestió dels permisos de càmera i la reproducció del vídeo capturat.

Primer s’ha implementat la gestió del permís de càmera. Android considera l’accés a la
càmera un permís “dangerous”, per tant no n’hi ha prou amb declarar-lo al AndroidManifest.xml,
sinó que també s’ha de demanar en temps d’execució. Per fer-ho s’utilitza
rememberLauncherForActivityResult amb ActivityResultContracts.RequestPermission().
Quan l’usuari prem el botó “Demanar permís de càmera”, es llança la sol·licitud del permís.
Si l’usuari l’accepta, la variable d’estat cameraPermissionGranted passa a ser true i l’aplicació
mostra la pantalla CameraScreen(). Si el permís no està concedit, es mostra un missatge i el
botó per tornar-lo a demanar.

El segon canvi és la captura i reproducció del vídeo. Per gravar el vídeo s’utilitza
ActivityResultContracts.CaptureVideo(), que obre l’aplicació de càmera del dispositiu.
Abans d’obrir-la es crea un fitxer .mp4 amb la funció createImageFile(), que genera un
nom i obté una Uri segura mitjançant FileProvider. Aquesta Uri és la ubicació on es guardarà
el vídeo gravat.

Un cop el vídeo s’ha capturat correctament, s’utilitza Media3 (ExoPlayer) per reproduir-lo
dins de la interfície de l’aplicació. Es crea un reproductor amb ExoPlayer.Builder(context),
es carrega el vídeo amb MediaItem.fromUri(uri), es prepara amb prepare() i es configura perquè
comenci a reproduir-se automàticament. Per mostrar el reproductor dins de Jetpack Compose
s’utilitza AndroidView, que permet inserir el component clàssic PlayerView dins de la UI de Compose.

Finalment s’ha afegit DisposableEffect per alliberar el reproductor quan el composable
desapareix. Això crida player.release() i evita que el reproductor quedi ocupant memòria o
recursos del sistema després de deixar de mostrar el vídeo.*/
