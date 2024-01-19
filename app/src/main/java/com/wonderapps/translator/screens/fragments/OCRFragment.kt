package com.wonderapps.translator.screens.fragments


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.theartofdev.edmodo.cropper.CropImageView
import com.wonderapps.translator.R
import com.wonderapps.translator.databinding.FragmentOCRBinding
import com.wonderapps.translator.screens.LoadImageActivity
import com.wonderapps.translator.top_level_functions.getLanguages_for_translate_fragment
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class OCRFragment : Fragment() {

    private var permissions = mutableListOf<String>()
    private var imageCapture: ImageCapture? = null
    private var click = 0
    private var storageIconClick = 0
    private lateinit var result: Task<Text>
    private lateinit var binding: FragmentOCRBinding
    private lateinit var ocrLanguageList: ArrayList<ConversationLanguage>
    private lateinit var sharedPreferencesOfOcrTextInEditText: SharedPreferences
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    companion object {

        lateinit var progressDialog: ProgressDialog
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        var storagePermissions = false
        var cameraPermissions = false
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_o_c_r, null, false)
        val view = binding.root
        click = 0

        removephonekeypadFromOcrFragment()




        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()


        startCamera()
        progressDialog = ProgressDialog(requireContext())
        setHasOptionsMenu(true)
        permissions = mutableListOf()

        binding.customOverlay.captureImageBtn
            .setOnClickListener {
                checkCameraPermissions()
                permissions = mutableListOf()
                if (cameraPermissions) {
                    click += 1
                    if (click == 1) {
                        takePhoto()
                    }
                } else {
                    checkCameraPermissions()
                }
            }



        binding.customOverlay.openGalleryIcon.setOnClickListener {

            checkStoragePermissions()
            permissions = mutableListOf()
            if (storagePermissions) {
                storageIconClick += 1
                if (storageIconClick == 1) {

                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, 100)
                }
            }
        }


        ocrLanguageList = ArrayList()
        getLanguages_for_translate_fragment(ocrLanguageList)
        sharedPreferencesOfOcrTextInEditText =
            requireContext().getSharedPreferences("text_on_edit_text_of_ocr", Context.MODE_PRIVATE)


        return view
    }

    private fun checkCameraPermissions() {
        Dexter.withContext(requireContext()).withPermission(Manifest.permission.CAMERA)
            .withListener(
                object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        cameraPermissions = true
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {

                        if (response.isPermanentlyDenied) {
                            cameraPermissions = false

                            val builder = AlertDialog.Builder(requireContext())
                            builder.setTitle(R.string.PermissionsNotGranted)
                            builder.setMessage(getString(R.string.CameraPermissionsMsg))
                            builder.setPositiveButton(R.string.GrantPermissionsBtn) { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri =
                                    Uri.fromParts("package", requireContext().packageName, null)
                                intent.data = uri
                                requireContext().startActivity(intent)
                            }
                            builder.setNegativeButton(getString(R.string.Cancel)) { _, _ ->
                                builder.setCancelable(true)
                            }
                            builder.create().show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest,
                        token: PermissionToken
                    ) {

                        token.continuePermissionRequest()
                    }
                }).check()
    }


    private fun checkStoragePermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(context).withPermission(Manifest.permission.READ_MEDIA_IMAGES)
                .withListener(
                    object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            storagePermissions = true
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {

                            if (response.isPermanentlyDenied) {
                                storagePermissions = false

                                val builder = AlertDialog.Builder(requireContext())
                                builder.setTitle(getString(R.string.PermissionsNotGranted))
                                builder.setMessage(getString(R.string.StoragePermissionsMsg))
                                builder.setPositiveButton(getString(R.string.GrantPermissionsBtn)) { _, _ ->
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri =
                                        Uri.fromParts("package", requireContext().packageName, null)
                                    intent.data = uri
                                    requireContext().startActivity(intent)
                                }
                                builder.setNegativeButton(getString(R.string.Cancel)) { _, _ ->
                                    builder.setCancelable(true)
                                }
                                builder.create().show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest,
                            token: PermissionToken
                        ) {

                            token.continuePermissionRequest()
                        }
                    }).check()
        } else {
            Dexter.withContext(context).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(
                    object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            storagePermissions = true
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {

                            if (response.isPermanentlyDenied) {

                                storagePermissions = false

                                val builder = AlertDialog.Builder(context)
                                builder.setTitle(getString(R.string.PermissionsNotGranted))
                                builder.setMessage(getString(R.string.StoragePermissionsMsg))
                                builder.setPositiveButton(R.string.GrantPermissionsBtn) { _, _ ->
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri =
                                        Uri.fromParts("package", requireContext().packageName, null)
                                    intent.data = uri
                                    requireContext().startActivity(intent)
                                }
                                builder.setNegativeButton(getString(R.string.Cancel)) { _, _ ->
                                    builder.setCancelable(true)
                                }
                                builder.create().show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest,
                            token: PermissionToken
                        ) {

                            token.continuePermissionRequest()
                        }
                    }).check()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("jnvruwjnupe", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun takePhoto() {

        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        val myProgressDialog = ProgressDialog(requireContext())
        myProgressDialog.setCancelable(false)
        myProgressDialog.setTitle("Processing your image")


        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {


                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        val savedUri = Uri.fromFile(photoFile)
                        CropImage.activity(savedUri).setGuidelines(CropImageView.Guidelines.ON)
                            .start(requireContext(), this@OCRFragment)
                        val msg = "Photo capture succeeded: ${output.savedUri}"

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )

    }


    override fun onResume() {
        super.onResume()
        super.onResume()

        click = 0
        storageIconClick = 0
    }

    @JvmName("getOutputDirectory1")
    fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireContext().filesDir
    }

    private fun removephonekeypadFromOcrFragment() {
        try {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 5000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Camera Permission Granted", Toast.LENGTH_LONG)
                    .show()
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                } else {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        } else if (requestCode == 7000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Storage Permission Granted", Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle(getString(R.string.GettingText))

        progressDialog.setCancelable(false)
        progressDialog.create()


        val sharedPreferences1 =
            requireContext().getSharedPreferences("OCR_LANGUAGE_PREFERENCE", Context.MODE_PRIVATE)

        val firstCardLanguage =
            sharedPreferences1.getString("OCR_FIRST_LANGUAGE_NAME", "").toString()

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == 100 && data?.data != null && (firstCardLanguage == "English" || firstCardLanguage == "Afrikaans" || firstCardLanguage == "Catalan" || firstCardLanguage == "Spanish" || firstCardLanguage == "Danish" || firstCardLanguage == "German" || firstCardLanguage == "French" || firstCardLanguage == "Albanian" || firstCardLanguage == "Haitian Creole" || firstCardLanguage == "Hungarian" || firstCardLanguage == "Indonesian" || firstCardLanguage == "Italian" || firstCardLanguage == "Latvian" || firstCardLanguage == "Welsh" || firstCardLanguage == "Turkish" || firstCardLanguage == "Slovenian" || firstCardLanguage == "Slovak" || firstCardLanguage == "Romanian" || firstCardLanguage == "Malay" || firstCardLanguage == "Portuguese" || firstCardLanguage == "Norwegian" || firstCardLanguage == "Maltese" || firstCardLanguage == "Lithuanian")) {
                CropImage.activity(data.data).setGuidelines(CropImageView.Guidelines.ON)
                    .start(requireContext(), this)

                click = 0
                storageIconClick = 0
                binding.ocrBackground.setBackgroundResource(R.drawable.ocr_loading_background)

            }

            if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE && (firstCardLanguage == "English" || firstCardLanguage == "Afrikaans" || firstCardLanguage == "Catalan" || firstCardLanguage == "Spanish" || firstCardLanguage == "Danish" || firstCardLanguage == "German" || firstCardLanguage == "French")) {
                progressDialog.show()
                val data1 = CropImage.getActivityResult(data)

                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = data1.uri

                    val recognizer =
                        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    val image = InputImage.fromFilePath(requireContext(), resultUri)

                    result = recognizer.process(image).addOnSuccessListener {

                        try {
                            val intent = Intent(requireContext(), LoadImageActivity::class.java)
                            intent.putExtra("crop_image_uri", data1.uri.toString())
                            intent.putExtra("data", data)
                            intent.putExtra("ocr_text_from_image", it.text)

                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Error a rha hy betta",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            progressDialog.cancel()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            progressDialog.dismiss()
            progressDialog.cancel()
        } catch (_: Exception) {
        }

    }

}


