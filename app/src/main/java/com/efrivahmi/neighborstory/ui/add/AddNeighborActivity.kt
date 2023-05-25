package com.efrivahmi.neighborstory.ui.add

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.efrivahmi.neighborstory.databinding.ActivityAddNeighborBinding
import com.efrivahmi.neighborstory.ui.main.MainActivity
import com.efrivahmi.neighborstory.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File



@Suppress("NAME_SHADOWING")
class AddNeighborActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNeighborBinding
    private lateinit var factory: NeighborFactory
    private val addViewModel: AddViewModel by viewModels { factory }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null
    private var getNeighborNew: File? = null

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getNeighborNew = myFile
            val result = rotateBitmapFromExif(this, Uri.fromFile(getNeighborNew))
            Toast.makeText(applicationContext, "Image taken successfully", Toast.LENGTH_SHORT).show()
            binding.uploadImage.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToNewFile(selectedImg, this)
            getNeighborNew = myFile
            binding.uploadImage.setImageURI(selectedImg)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNeighborBinding.inflate(layoutInflater)
        setContentView(binding.root)
        factory = NeighborFactory.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding?.apply {
            camera.setOnClickListener { startTakePhoto() }
            gallery.setOnClickListener { startGallery() }
            upload.setOnClickListener { uploadImage() }
        }

        getLocation()
        showLoading()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createNewFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "com.efrivahmi.neighborstory",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    this.location = location
                    Log.d("AddNeighborStory", "getLastLocation: ${location.latitude}, ${location.longitude}")
                } else {
                    Toast.makeText(
                        this,
                        "Location Error",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.checkBox.isChecked = false
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("AddStory", "$permissions")
        when {
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getLocation()
            }
            else -> {
                Snackbar
                    .make(
                        binding.root,
                        "Location Error",
                        Snackbar.LENGTH_SHORT
                    )
                    //.setActionTextColor(getColor(R.color.white))
                    .setAction("Switch Button") {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                            val uri = Uri.fromParts("uri", packageName, null)
                            intent.data = uri
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                    .show()
                binding.checkBox.isChecked = false
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private lateinit var currentPhotoPath: String

    private fun uploadImage() {
        addViewModel.getNeighbor().observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val neighborModel = result.data
                    if (getNeighborNew != null) {
                        val file = reduceImage(getNeighborNew as File)
                        val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
                        val imageMultipart = MultipartBody.Part.createFormData(
                            "photo",
                            file.name,
                            requestImageFile
                        )
                        uploadResponse(
                            neighborModel.token,
                            imageMultipart,
                            binding.description.text.toString().toRequestBody("text/plain".toMediaType())
                        )
                    } else if (!toastDisplayed) {
                        Toast.makeText(applicationContext, "Input Image First", Toast.LENGTH_SHORT).show()
                        toastDisplayed = true
                    }
                }
                is Result.Error -> {
                    val errorMessage = result.error
                    showErrorDialog(errorMessage)
                }
                Result.Loading -> {
                    showLoading()
                }
            }
        }
    }

    private var toastDisplayed = false

    private fun uploadResponse(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody
    ) {
        addViewModel.uploadStory(token, file, description)
        addViewModel.upload.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                is Result.Error -> {
                    val errorMessage = result.error
                    showErrorDialog(errorMessage)
                }
                Result.Loading -> {
                    finish()
                }
            }
        }
        showToast()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Toast.makeText(
                    applicationContext,
                    "Camera permission granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun showLoading() {
        addViewModel.isLoading.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val isLoading = result.data
                    binding.progressBar5.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
                is Result.Error -> {
                    val errorMessage = result.error
                    showErrorDialog(errorMessage)
                    binding.progressBar5.visibility = View.GONE
                }
                Result.Loading -> {
                    binding.progressBar5.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Error")
        dialogBuilder.setMessage(errorMessage)
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun showToast() {
        addViewModel.toast.observe(this) {
            it.getContentIfNotHandled()?.let { toastText ->
                Toast.makeText(
                    this, toastText, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}