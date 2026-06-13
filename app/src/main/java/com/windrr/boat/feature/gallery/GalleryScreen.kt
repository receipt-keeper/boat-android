package com.windrr.boat.feature.gallery

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.windrr.boat.core.util.createImageCaptureUri

/**
 * 사진 선택 화면 — 갤러리 다중선택 또는 카메라 촬영으로 최대 5장 첨부
 *
 * - 추가 타일 → 갤러리/카메라 선택 다이얼로그
 * - 갤러리: Photo Picker (권한 불필요)
 * - 카메라: CAMERA 권한 요청 후 FileProvider URI로 촬영
 * - 각 사진 우측 상단의 ✕ 버튼으로 삭제
 *
 * @param viewModel 사진 선택 ViewModel
 * @param onBack 뒤로가기 콜백
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showSourceDialog by remember { mutableStateOf(false) }
    // 카메라 촬영 결과가 저장될 URI (촬영 직전에 생성, 프로세스 죽음에도 보존)
    var cameraImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // 에러 발생 시 토스트 표시 후 소비
    LaunchedEffect(state.error) {
        state.error?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.handleIntent(GalleryIntent.ClearError)
        }
    }

    // 갤러리 다중선택 (최대 MAX_PHOTOS장)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(GalleryState.MAX_PHOTOS)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.handleIntent(GalleryIntent.AddPhotos(uris))
        }
    }

    // 카메라 촬영 — 성공 시 미리 만들어 둔 URI를 추가
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        val uri = cameraImageUri
        if (isSuccess && uri != null) {
            viewModel.handleIntent(GalleryIntent.AddPhotos(listOf(uri)))
        }
    }

    fun launchCamera() {
        val uri = context.createImageCaptureUri()
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    // 카메라 권한 — 허용되면 즉시 촬영 실행
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) launchCamera()
    }

    fun onPickFromGallery() {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun onTakePhoto() {
        if (cameraPermissionState.status.isGranted) {
            launchCamera()
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(text = "← 뒤로", color = Color.Black)
            }
            Text(
                text = "사진 (${state.photos.size}/${GalleryState.MAX_PHOTOS})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.photos) { uri ->
                PhotoThumbnail(
                    uri = uri,
                    onRemove = { viewModel.handleIntent(GalleryIntent.RemovePhoto(uri)) }
                )
            }
            // 추가 타일 — 아직 5장 미만일 때만 노출
            if (state.canAddMore) {
                item {
                    AddPhotoTile(onClick = { showSourceDialog = true })
                }
            }
        }
    }

    // 갤러리/카메라 선택 다이얼로그
    if (showSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showSourceDialog = false },
            onGallery = {
                showSourceDialog = false
                onPickFromGallery()
            },
            onCamera = {
                showSourceDialog = false
                onTakePhoto()
            }
        )
    }
}

/**
 * 선택된 사진 1장 썸네일 + 우측 상단 삭제 버튼
 *
 * @param uri 표시할 사진 URI
 * @param onRemove 삭제 버튼 클릭 콜백
 */
@Composable
private fun PhotoThumbnail(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )
        // 삭제 버튼 (✕)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 사진 추가 타일 (점선 테두리 + ＋)
 *
 * @param onClick 클릭 콜백
 */
@Composable
private fun AddPhotoTile(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "＋", fontSize = 28.sp, color = Color.Gray)
            Text(text = "사진 추가", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

/**
 * 사진 소스 선택 다이얼로그 (갤러리 / 카메라)
 *
 * @param onDismiss 다이얼로그 닫기 콜백
 * @param onGallery 갤러리 선택 콜백
 * @param onCamera 카메라 선택 콜백
 */
@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("사진 추가") },
        text = { Text("사진을 어떻게 추가할까요?") },
        confirmButton = {
            TextButton(onClick = onGallery) {
                Text("갤러리에서 선택")
            }
        },
        dismissButton = {
            TextButton(onClick = onCamera) {
                Text("카메라로 촬영")
            }
        }
    )
}
