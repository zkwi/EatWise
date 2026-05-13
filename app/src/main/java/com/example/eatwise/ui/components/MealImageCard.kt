package com.example.eatwise.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.eatwise.core.storage.GalleryImageSaver
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.theme.LineSoft
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MealImageCard(
    imagePath: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    var showPreview by remember(imagePath) { mutableStateOf(false) }

    Card(
        onClick = { showPreview = true },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, LineSoft.copy(alpha = 0.62f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.85f),
        )
    }

    if (showPreview) {
        MealImagePreviewDialog(
            imagePath = imagePath,
            contentDescription = contentDescription,
            onDismiss = { showPreview = false },
        )
    }
}

@Composable
private fun MealImagePreviewDialog(
    imagePath: String,
    contentDescription: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    var pendingSavePath by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val path = pendingSavePath
        pendingSavePath = null
        if (granted && path != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) { GalleryImageSaver.save(context, path) }
                Toast.makeText(context, if (saved) strings.imageSavedToGallery else strings.imageSaveFailed, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, strings.storagePermissionDenied, Toast.LENGTH_SHORT).show()
        }
    }

    fun saveImage() {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingSavePath = imagePath
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        scope.launch {
            val saved = withContext(Dispatchers.IO) { GalleryImageSaver.save(context, imagePath) }
            Toast.makeText(context, if (saved) strings.imageSavedToGallery else strings.imageSaveFailed, Toast.LENGTH_SHORT).show()
        }
    }

    var scale by remember(imagePath) { mutableFloatStateOf(1f) }
    var offset by remember(imagePath) { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (scale * zoomChange).coerceIn(1f, 4f)
        scale = nextScale
        offset = if (nextScale == 1f) Offset.Zero else offset + panChange
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    )
                    .transformable(transformState)
                    .pointerInput(imagePath) {
                        detectTapGestures(onLongPress = { saveImage() })
                    },
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = ::saveImage,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.Black.copy(alpha = 0.46f), CircleShape),
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = strings.saveImage, tint = Color.White)
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.Black.copy(alpha = 0.46f), CircleShape),
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.back, tint = Color.White)
                }
            }
        }
    }
}
