package com.warden.app.ui.unlock

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.warden.app.databinding.ActivityCameraUnlockBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraUnlockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BREAK_MINUTES = "break_minutes"
    }

    private lateinit var binding: ActivityCameraUnlockBinding
    private val viewModel: CameraUnlockViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupUI()
        startCamera()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.btnCapture.setOnClickListener {
            takePhoto()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.reset()
            binding.layoutResult.visibility = View.GONE
            binding.previewView.visibility = View.VISIBLE
            binding.btnCapture.isEnabled = true
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "CAMERA FAILED: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        binding.btnCapture.isEnabled = false
        binding.tvStatus.text = "CAPTURING..."

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    image.close()
                    binding.ivCaptured.setImageBitmap(bitmap)
                    binding.previewView.visibility = View.GONE
                    binding.layoutResult.visibility = View.VISIBLE
                    viewModel.analyzePhoto(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    binding.btnCapture.isEnabled = true
                    binding.tvStatus.text = "CAPTURE FAILED. TRY AGAIN."
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        // Rotate if needed
        val rotation = image.imageInfo.rotationDegrees
        return if (rotation != 0) {
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotation.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else bitmap
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is CameraUnlockViewModel.UnlockState.Idle -> {
                    binding.tvStatus.text = getString(com.warden.app.R.string.camera_unlock_subtitle)
                    binding.progressBar.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE
                }
                is CameraUnlockViewModel.UnlockState.Analyzing -> {
                    binding.tvStatus.text = getString(com.warden.app.R.string.analyzing)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.GONE
                }
                is CameraUnlockViewModel.UnlockState.Accepted -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvStatus.text = getString(com.warden.app.R.string.unlock_granted)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(this, com.warden.app.R.color.accent_green))
                    binding.btnRetry.visibility = View.GONE

                    // Return success to LockOverlayActivity
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_BREAK_MINUTES, state.breakMinutes)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                is CameraUnlockViewModel.UnlockState.Rejected -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvStatus.text = getString(com.warden.app.R.string.unlock_denied)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(this, com.warden.app.R.color.accent_red))
                    binding.btnRetry.visibility = View.VISIBLE
                }
                is CameraUnlockViewModel.UnlockState.NoApiKey -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvStatus.text = getString(com.warden.app.R.string.no_api_key)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(this, com.warden.app.R.color.accent_red))
                    binding.btnRetry.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
