@file:kotlin.OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)

package com.example.trackpoints.presentation.auth.sign_up

import android.Manifest
import android.content.Context
import android.graphics.RectF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trackpoints.ui.theme.AppFonts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.also
import kotlin.apply
import kotlin.collections.drop
import kotlin.collections.first
import kotlin.collections.firstOrNull
import kotlin.collections.isNotEmpty
import kotlin.to


data class VerifyFaceUiState(
    val checks: List<LivenessCheck> = emptyList(),
    val message: String = "Please position your face",
    val progress: Float = 0f,
    val isFaceChecked: Boolean = false,
    val verificationComplete: Boolean = false,
)

enum class LivenessCheck(val instruction: String) {
    BLINK("Blink your Eyes"), LOOK_LEFT("Look Left"), LOOK_RIGHT("Look Right")
}


class VerifyFaceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VerifyFaceUiState())
    val uiState: StateFlow<VerifyFaceUiState> = _uiState.asStateFlow()
    private var totalChecks: Int = 0
    private var isTransitioning = false
    private val faceDetector: FaceDetector
    private var didEyesOpen = true

    companion object {
        private const val EULER_Y_THRESHOLD = 20f
        private const val BLINK_THRESHOLD = 0.3f
    }

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build()
        faceDetector = FaceDetection.getClient(options)
    }

    fun startFaceVerification() {
        val challenges = generateChecks()
        totalChecks = challenges.size

        _uiState.update {
            it.copy(
                message = "Starting Face Check...",
                progress = 0f,
                checks = challenges,
                verificationComplete = false
            )
        }
    }

    private fun generateChecks(): List<LivenessCheck> {
        return listOf(LivenessCheck.LOOK_RIGHT, LivenessCheck.LOOK_LEFT, LivenessCheck.BLINK)
    }

    fun onNoFaceChecked() {
        _uiState.update { it.copy(isFaceChecked = false) }
        if (_uiState.value.checks.isNotEmpty()) {
            _uiState.update { it.copy(message = "Please position your face") }
        }
    }

    fun onFaceChecked(face: Face) {
        if (_uiState.value.progress == 1f || isTransitioning) return
        _uiState.update { it.copy(isFaceChecked = true) }

        val currentChallenge = _uiState.value.checks.firstOrNull()
        if (currentChallenge == null || _uiState.value.verificationComplete) return

        _uiState.update { it.copy(message = currentChallenge.instruction) }

        var checkMet = false
        when (currentChallenge) {
            LivenessCheck.BLINK -> {
                val leftEyeOpen = face.leftEyeOpenProbability
                val rightEyeOpen = face.rightEyeOpenProbability

                if (leftEyeOpen == null || rightEyeOpen == null) return

                if (didEyesOpen && leftEyeOpen < BLINK_THRESHOLD && rightEyeOpen < BLINK_THRESHOLD) {
                    checkMet = true
                    didEyesOpen = false
                } else if (leftEyeOpen > 0.7 && rightEyeOpen > 0.7) {
                    didEyesOpen = true
                }
            }

            LivenessCheck.LOOK_LEFT -> if (face.headEulerAngleY > EULER_Y_THRESHOLD) checkMet = true
            LivenessCheck.LOOK_RIGHT -> if (face.headEulerAngleY < -EULER_Y_THRESHOLD) checkMet =
                true
        }

        if (checkMet && !isTransitioning) {
            isTransitioning = true
            _uiState.update { it.copy(message = "Good") }
            viewModelScope.launch {
                moveToNextCheck()
                isTransitioning = false
            }
        }
    }

    private suspend fun moveToNextCheck() {
        val remainingChecks = _uiState.value.checks.drop(1)

        if (remainingChecks.isEmpty()) {
            _uiState.update {
                it.copy(
                    progress = 1f, verificationComplete = true,
                    message = "Face Verification Done!", checks = emptyList()
                )
            }
            delay(2000)
            _uiState.update { it.copy(message = "Proceeding to sign up screen...") }
        } else {
            val progress = 1f - (remainingChecks.size.toFloat() / totalChecks)
            _uiState.update {
                it.copy(progress = progress)
            }
            delay(2500)
            _uiState.update {
                it.copy(
                    message = remainingChecks.first().instruction, checks = remainingChecks
                )
            }
        }
    }

    override fun onCleared() {
        faceDetector.close()
        super.onCleared()
    }
}


@Composable
fun VerifyFaceScreen(
    viewModel: SignUpViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SignUpEffect.NavigateToHome -> {}
                SignUpEffect.NavigateBack -> onNavigateBack()
                SignUpEffect.NavigateToLogin -> {}
                SignUpEffect.NavigateToNext -> onNavigateNext()
            }
        }
    }

    VerifyFaceContent(
        state = state,
        onIntent = viewModel::handleIntent,
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VerifyFaceContent(
    humanVerificationViewModel: VerifyFaceViewModel = viewModel(),
    state: SignUpState, onIntent: (SignUpIntent) -> Unit,
) {
    val uiState by humanVerificationViewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message == "Proceeding to sign up screen...") {
            delay(1000)
            onIntent(SignUpIntent.NextOfVerificationClicked)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (cameraPermissionState.status.isGranted) {
                    LaunchedEffect(Unit) {
                        humanVerificationViewModel.startFaceVerification()
                    }

                    CameraPreviewComposable(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        viewModel = humanVerificationViewModel
                    )
                    BoxWithConstraints(
                        Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFEF3E2))
                    ) {
                        FaceOverlay(
                            modifier = Modifier.fillMaxSize(),
                            progress = uiState.progress,
                            isFaceDetected = uiState.isFaceChecked,
                        )
                        val yOffset = maxHeight * 0.05f
                        val newCenterY = (maxHeight / 2) - yOffset
                        val ovalHalfHeight = maxHeight * 0.25f

                        val ovalBottom = newCenterY + ovalHalfHeight

                        Text(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = ovalBottom + 32.dp)
                                .padding(horizontal = 70.dp),
                            text = uiState.message,
                            color = Color(0xFF1E1E1E),
                            fontFamily = AppFonts.robotoCondensed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (cameraPermissionState.status.shouldShowRationale) {
                                "Camera permission is required"
                            } else {
                                "Grant camera permission in settings."
                            }, textAlign = TextAlign.Center
                        )
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFA812F)
                            ), onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Grant Camera Permission")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreviewComposable(
    modifier: Modifier = Modifier, viewModel: VerifyFaceViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = {
            val previewView = PreviewView(it).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            startFaceCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                onFaceAnalyzed = viewModel::onFaceChecked,
                onNoFaceDetected = viewModel::onNoFaceChecked
            )
            previewView
        }, modifier = modifier
    )
}

private fun startFaceCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onFaceAnalyzed: (Face) -> Unit,
    onNoFaceDetected: () -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val cameraExecutor = Executors.newSingleThreadExecutor()

    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build()
    val faceDetector = FaceDetection.getClient(options)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val imageAnalyzer =
            ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(
                        cameraExecutor, FaceAnalyzer(
                            faceDetector, onFaceAnalyzed, onNoFaceDetected
                        )
                    )
                }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalyzer
            )
        } catch (exc: Exception) {
        }
    }, ContextCompat.getMainExecutor(context))
}

@OptIn(ExperimentalGetImage::class)
private class FaceAnalyzer(
    private val detector: FaceDetector,
    private val onFaceAnalyzed: (Face) -> Unit,
    private val onNoFaceDetected: () -> Unit
) : ImageAnalysis.Analyzer {

    private val Y_OFFSET_PERCENT = 0.05f
    private val HALF_WIDTH_PERCENT = 0.45f
    private val HALF_HEIGHT_PERCENT = 0.25f

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            val imageWidth = imageProxy.height
            val imageHeight = imageProxy.width

            val newCenterY = (imageHeight / 2f) - (imageHeight * Y_OFFSET_PERCENT)
            val halfWidth = imageWidth * HALF_WIDTH_PERCENT
            val halfHeight = imageHeight * HALF_HEIGHT_PERCENT

            val activeRect = RectF(
                (imageWidth / 2f) - halfWidth,
                newCenterY - halfHeight,
                (imageWidth / 2f) + halfWidth,
                newCenterY + halfHeight
            )

            detector.process(image).addOnSuccessListener { faces ->
                val centeredFace =
                    faces.firstOrNull { face -> activeRect.contains(RectF(face.boundingBox)) }
                if (centeredFace != null) onFaceAnalyzed(centeredFace)
                else onNoFaceDetected()
            }.addOnFailureListener { e -> Log.e("Face Check", "Face check failed", e) }
                .addOnCompleteListener { imageProxy.close() }
        }
    }
}

@Composable
fun FaceOverlay(
    modifier: Modifier = Modifier,
    isFaceDetected: Boolean,
    progress: Float,
) {
    Canvas(modifier = modifier) {
        val yOffset = size.height * 0.05f
        val newCenterY = center.y - yOffset

        val halfWidth = size.width * 0.45f
        val halfHeight = size.height * 0.25f

        val ovalRect = Rect(
            left = center.x - halfWidth,
            top = newCenterY - halfHeight,
            right = center.x + halfWidth,
            bottom = newCenterY + halfHeight
        )

        drawOval(
            color = Color.Transparent,
            topLeft = ovalRect.topLeft,
            size = ovalRect.size,
            blendMode = BlendMode.Clear
        )

        if (isFaceDetected || progress == 1f) {
            val progressAngle = progress * 360f
            drawArc(
                color = Color(0xFFFA812F),
                startAngle = -90f,
                sweepAngle = progressAngle,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx()),
                topLeft = ovalRect.topLeft,
                size = ovalRect.size
            )
        }
    }
}
