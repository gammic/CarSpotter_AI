package com.example.carspotter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var txtResult: TextView
    private lateinit var txtConfidence: TextView
    private lateinit var btnDetails: Button
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var carClassifier: CarClassifier

    private var currentBitmap: Bitmap? = null

    // --- GESTIONE INPUT (CAMERA & GALLERIA) ---

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { mostraERiconosci(it) }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                mostraERiconosci(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Errore caricamento immagine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) cameraLauncher.launch(null) else showSettingsDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
        carClassifier = CarClassifier(this)
        checkDatabaseStatus()
        setupListeners()
    }

    private fun initUI() {
        imgPreview = findViewById(R.id.imgPreview)
        txtResult = findViewById(R.id.txtResult)
        txtConfidence = findViewById(R.id.txtConfidence)
        btnDetails = findViewById(R.id.btnDetails)
        loadingOverlay = findViewById(R.id.loadingOverlay)
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnCamera).setOnClickListener {
            handleCameraPermission()
        }

        findViewById<Button>(R.id.btnGallery).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        findViewById<ImageButton>(R.id.btnSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnGarage).setOnClickListener {
            startActivity(Intent(this, GarageActivity::class.java))
        }

        btnDetails.setOnClickListener {
            saveToGarageAndOpenDetail()
        }
    }

    /**
     * Controlla se il database è pronto o deve essere popolato.
     */
    private fun checkDatabaseStatus() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val txtProgress = findViewById<TextView>(R.id.txtProgress)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext, this) { progress ->
                runOnUiThread {
                    loadingOverlay.visibility = View.VISIBLE
                    progressBar.progress = progress
                    txtProgress.text = "Caricamento Dataset: $progress%"
                    if (progress >= 100) {
                        loadingOverlay.postDelayed({ loadingOverlay.visibility = View.GONE }, 500)
                    }
                }
            }
            if (db.carDao().getCount() > 0) {
                withContext(Dispatchers.Main) { loadingOverlay.visibility = View.GONE }
            }
        }
    }

    private fun mostraERiconosci(bitmap: Bitmap) {
        // Normalizzazione bitmap per TFLite
        val safeBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        currentBitmap = safeBitmap

        imgPreview.setImageBitmap(safeBitmap)
        findViewById<LinearLayout>(R.id.layoutPlaceholder).visibility = View.GONE
        imgPreview.visibility = View.VISIBLE

        if (::carClassifier.isInitialized) {
            val result = carClassifier.classify(safeBitmap).split("|")
            val carName = result[0]
            val accuracy = if (result.size > 1) result[1] else "0%"

            txtResult.text = carName
            txtConfidence.text = "Affidabilità: $accuracy"
            btnDetails.visibility = View.VISIBLE
            btnDetails.text = "VEDI SCHEDA $carName"
        }
    }

    private fun saveToGarageAndOpenDetail() {
        val name = txtResult.text.toString()
        currentBitmap?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                val path = saveImageToInternalStorage(it)
                val db = AppDatabase.getDatabase(applicationContext, this)
                db.carDao().addToGarage(ScannedCar(modelFamily = name, timestamp = System.currentTimeMillis(), imagePath = path))
            }
        }
        val intent = Intent(this, CarDetailActivity::class.java).apply {
            putExtra("CAR_FAMILY", name)
        }
        startActivity(intent)
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val filename = "car_${System.currentTimeMillis()}.jpg"
        openFileOutput(filename, Context.MODE_PRIVATE).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return getFileStreamPath(filename).absolutePath
    }

    private fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                cameraLauncher.launch(null)
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permesso Fotocamera")
            .setMessage("Attiva i permessi nelle impostazioni per usare la fotocamera.")
            .setPositiveButton("IMPOSTAZIONI") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Annulla", null).show()
    }
}