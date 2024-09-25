import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import android.database.Cursor
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.core.content.FileProvider
import com.ufg.criptoapplication.R
import com.ufg.criptoapplication.compose.CameraView
import java.io.File
import java.util.concurrent.Executors
import androidx.compose.ui.Alignment

@Composable
fun Parcial2Screen() {

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var lastImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String>("") }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var storagePermissionGranted by remember { mutableStateOf(false) }
    var coordinates by remember { mutableStateOf<String?>(null) }
    var shouldShowCamera by remember { mutableStateOf(false) }
    var shouldShowPhoto by remember { mutableStateOf(false) }

    // Contexto y permisos
    val context = LocalContext.current
    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    val cameraPermission = Manifest.permission.CAMERA
    val storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE

    // Función para obtener el nombre del archivo
    fun getFileName(uri: Uri): String? {
        val contentResolver = context.contentResolver
        var name: String? = null
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }

    // Lanzador para seleccionar un archivo
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileName = getFileName(it) ?: "Archivo no encontrado"
        }
    }

    // Lanzadores para solicitar permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        locationPermissionGranted = isGranted
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        cameraPermissionGranted = isGranted
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        storagePermissionGranted = isGranted
    }

    // Comprobar permisos al cargar la pantalla
    LaunchedEffect(Unit) {
        locationPermissionGranted = ContextCompat.checkSelfPermission(context, locationPermission) == PackageManager.PERMISSION_GRANTED
        cameraPermissionGranted = ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED
        storagePermissionGranted = ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED
    }

    // Función para obtener ubicación usando LocationManager
    fun getLocation() {
        if (locationPermissionGranted) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                coordinates = location?.let { "Lat: ${it.latitude}, Lon: ${it.longitude}" } ?: "No se pudo obtener la ubicación"
            }
        } else {
            locationPermissionLauncher.launch(locationPermission)
        }
    }

    // Directorio de salida para la cámara
    val outputDirectory = getOutputDirectory(context)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado: Estado de permisos
        Text(
            text = "Estado de Permisos",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Mostrar estado de permisos con colores y estilos
        PermissionStatus("Permiso GPS", locationPermissionGranted)
        PermissionStatus("Permiso Almacenamiento", storagePermissionGranted)
        PermissionStatus("Permiso Cámara", cameraPermissionGranted)

        // Botón para obtener ubicación GPS
        Button(
            onClick = { getLocation() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Obtener Ubicación GPS")
        }

        // Contenedor copiable de coordenadas
        TextField(
            value = coordinates ?: "Coordenadas no disponibles",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            textStyle = LocalTextStyle.current.copy(color = Color.Gray)
        )

        // Botón para seleccionar un archivo
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    storagePermissionGranted = true
                    fileLauncher.launch("*/*")
                } else {
                    if (storagePermissionGranted) {
                        fileLauncher.launch("*/*")
                    } else {
                        storagePermissionLauncher.launch(storagePermission)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Seleccionar Archivo")
        }

        // Mostrar el nombre del archivo seleccionado
        if (selectedFileName.isNotEmpty()) {
            Text(
                text = "Archivo seleccionado: $selectedFileName",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Botón para tomar una foto
        Button(
            onClick = {
                if (cameraPermissionGranted) {
                    shouldShowCamera = true
                } else {
                    cameraPermissionLauncher.launch(cameraPermission)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Tomar Foto")
        }

        // Mostrar cámara si debe mostrarse
        if (shouldShowCamera) {
            CameraView(
                outputDirectory = outputDirectory,
                executor = cameraExecutor,
                onImageCaptured = { uri ->
                    cameraImageUri = uri
                    lastImageUri = uri
                    shouldShowCamera = false
                    shouldShowPhoto = true
                },
                onError = { /* Manejar error */ }
            )
        }

        // Mostrar la imagen capturada
        if (shouldShowPhoto && lastImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(lastImageUri),
                contentDescription = "Imagen Capturada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }

}

@Composable
fun PermissionStatus(permissionName: String, isGranted: Boolean) {
    val textColor = if (isGranted) Color.Green else Color.Red
    val backgroundColor = Color.Black
    val text = if (isGranted) "Concedido" else "Denegado"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        Text(
            text = "$permissionName: $text",
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun getOutputDirectory(context: Context): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}

