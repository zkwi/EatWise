package com.example.eatwise.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.eatwise.core.storage.ImageStorage
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraScreen(
    imageStorage: ImageStorage,
    onBack: () -> Unit,
    onImageReady: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
                if (!granted) errorMessage = "需要相机权限才能拍照分析。"
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PreviewView(viewContext).also { previewView ->
                        val providerFuture = ProcessCameraProvider.getInstance(viewContext)
                        providerFuture.addListener(
                            {
                                val provider = providerFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val capture = ImageCapture.Builder().build()
                                imageCapture = capture
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    capture,
                                )
                            },
                            ContextCompat.getMainExecutor(viewContext),
                        )
                    }
                },
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("拍一张餐食照", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 56.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("把整餐放进框内", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text("主食、配菜和饮料都拍到，结果更准确", color = Color.White.copy(alpha = 0.78f))
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                errorMessage?.let {
                    Text(it, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))
                }
                if (!hasPermission) {
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("开启相机权限")
                    }
                } else {
                    Button(
                        onClick = {
                            val capture = imageCapture ?: return@Button
                            isCapturing = true
                            errorMessage = null
                            val file = imageStorage.createCameraImageFile()
                            val output = ImageCapture.OutputFileOptions.Builder(file).build()
                            capture.takePicture(
                                output,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        onImageReady(file.absolutePath)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        errorMessage = "拍照失败，请再试一次。"
                                    }
                                },
                            )
                        },
                        enabled = imageCapture != null && !isCapturing,
                        modifier = Modifier.size(84.dp).clip(CircleShape),
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(30.dp))
                        } else {
                            Icon(Icons.Rounded.Camera, contentDescription = "拍照", modifier = Modifier.size(34.dp))
                        }
                    }
                }
            }
        }
    }
}
