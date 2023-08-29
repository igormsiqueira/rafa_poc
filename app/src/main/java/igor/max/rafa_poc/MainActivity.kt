package igor.max.rafa_poc

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment.*
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import igor.max.rafa_poc.Action.*
import igor.max.rafa_poc.ui.theme.Rafa_POCTheme
import java.io.File
import java.io.IOException
import java.time.LocalDateTime


class MainActivity : ComponentActivity() {
    private val pickedUris = mutableStateOf<List<Uri>?>(null)
    private val createdDoc = mutableStateOf<String?>(null)
    private val cameraFiles = mutableListOf<Uri>()
    private val resultCamera = mutableStateOf<Boolean>(false)

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(30)) { uris ->
            if (uris.isNotEmpty()) {
                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
                pickedUris.value = uris
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    private val launcher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        resultCamera.value = it
    }


    @Throws(IOException::class)
    private fun createFileForBitmap(): Uri {
        val storageDir = getExternalFilesDir(DIRECTORY_DOCUMENTS)

        val f = File(storageDir, "/${LocalDateTime.now()}-image.jpg")
        f.createNewFile()
        return FileProvider.getUriForFile(
            applicationContext,
            "igor.max.rafa_poc.provider", f
        )

    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            setPermissions()


            val menuItems = mutableListOf(
//                Action.GALLERY to painterResource(id = R.drawable.baseline_collections_24),
//                Action.CAMERA to painterResource(id = R.drawable.baseline_camera_alt_24),
                Action.FILE to painterResource(id = R.drawable.baseline_picture_as_pdf_24)
            )

            Rafa_POCTheme {
                val showLoading = remember { mutableStateOf(false) }
                val collectedFiles = remember { mutableListOf<Uri>() }

                Scaffold(topBar = {
                    TopAppBar(
                        title = {
                            Text("RAFA POC")
                        }
                    )
                },
                    bottomBar = {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ) {
                            menuItems.forEach {
                                NavigationBarItem(
                                    selected = false,
                                    onClick = {
                                        showLoading.value = true
                                        startPdfCreation(collectedFiles) {
                                            showLoading.value = false
                                        }
                                    },
                                    label = {
                                        Text(text = "Create PDF File")
                                    },
                                    icon = {
                                        Icon(it.second, "null")
                                    }
                                )
                            }
                        }
                    }) { it ->


                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        pickedUris.value?.let { allPicks ->
                            collectedFiles.addAll(allPicks)
                            pickedUris.value = mutableListOf()
                        }
                        resultCamera.value.let {
                            if (cameraFiles.isNotEmpty()) {
                                collectedFiles.add(
                                    cameraFiles.last()
                                )
                            }
                            cameraFiles.clear()
                            resultCamera.value = false
                        }

                        Box(
                            contentAlignment = Alignment.BottomCenter
                        ) {

                            showLoading.value.let {
                                if (it) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(top = 220.dp)
                                            .zIndex(13f)
                                    )
                                }
                            }
                            Column(
                                Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.Top
                            ) {
                                LazyVerticalGrid(
                                    contentPadding = PaddingValues(
                                        start = 12.dp,
                                        top = 16.dp,
                                        end = 12.dp,
                                        bottom = 16.dp
                                    ),
                                    columns = GridCells.Adaptive(minSize = 80.dp)

                                ) {
                                    itemsIndexed(collectedFiles) { index, image ->
                                        Thumbnail(
                                            index,
                                            image.toThumbNail(LocalContext.current).asImageBitmap()
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                PickPhotosButton {
                                    startPhotoPicker()
                                }
                                TakeAPhotoButton {
                                    val file = createFileForBitmap()
                                    cameraFiles.add(file)
                                    launcher.launch(file)
                                }
                            }
                        }

                    }
                }
            }

        }
    }

    private fun setPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(WRITE_EXTERNAL_STORAGE, CAMERA),
            0
        )

    }

    private fun executeAction(collectedFiles: MutableList<Uri>, action: Action) {
        when (action) {
            Action.CAMERA -> {
                val file = createFileForBitmap()
                cameraFiles.add(file)
                launcher.launch(file)
            }

            Action.GALLERY -> startPhotoPicker()
            FILE -> startPdfCreation(collectedFiles)
        }
    }

    private fun startPdfCreation(collectedFiles: MutableList<Uri>, function: () -> Unit = {}) {
        val pdfMakingStrategy = AndroidDependentPdfStrategy(application.contentResolver)
        val finalFolderPath = getExternalFilesDir(DIRECTORY_DOCUMENTS)?.path.orEmpty()

        PdfMaker(
            finalFolderPath,
            pdfMakingStrategy
        ).createPdf(collectedFiles) { resultFileUrl ->
            createdDoc.value = resultFileUrl
            val file = File(resultFileUrl)
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            runOnUiThread {
                startActivity(intent)
                collectedFiles.clear()
                function.invoke()
            }
        }

    }

    private fun startPhotoPicker() {
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

}

@Composable
private fun Thumbnail(index: Int, imageBitmap: ImageBitmap) {
    Box(
        contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(4.dp)
    ) {

        Image(
            bitmap = imageBitmap,
            contentDescription = "",
            modifier = Modifier
                .size(96.dp)
                .padding(8.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(CircleShape)
                .padding(4.dp)
                .background(color = Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Page $index", fontSize = 12.sp)
        }
    }
}

@Composable
fun PickPhotosButton(function: () -> Unit) {
    Button(
        modifier = Modifier.wrapContentSize(),
        onClick = { function() }) {
        Text(
            text = "Pick photos",
        )
    }
}

@Composable
fun TakeAPhotoButton(function: () -> Unit) {
    Button(
        modifier = Modifier.wrapContentSize(),
        onClick = { function.invoke() }) {
        Text(text = "Take a picture")
    }

}

enum class Action {
    CAMERA,
    GALLERY,
    FILE
}

private fun Uri.toThumbNail(context: Context): Bitmap {
    val ca = CancellationSignal()
    val size = android.util.Size(640, 480)
    return context.contentResolver.loadThumbnail(this, size, ca)
}

