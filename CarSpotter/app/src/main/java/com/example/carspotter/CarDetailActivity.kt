package com.example.carspotter

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent

class CarDetailActivity : AppCompatActivity() {

    private lateinit var carVersions: List<CarEntity>
    private lateinit var txtBrand: TextView
    private lateinit var txtModel: TextView
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_detail)

        txtBrand = findViewById(R.id.txtBrandDetail)
        txtModel = findViewById(R.id.txtModelDetail)
        spinner = findViewById(R.id.spinnerVersions)

        val familyName = intent.getStringExtra("CAR_FAMILY") ?: return
        txtModel.text = familyName
        // Caricamento immagine auto
        // 1. Otteniamo l'ID della risorsa di drawable
        val imageName = familyName.lowercase().replace(" ", "_").replace("-", "_")
        // 2. Cerchiamo l'ID della risorsa nella cartella drawable
        val resourceId = resources.getIdentifier(
            imageName,       // Nome del file (senza estensione)
            "drawable",      // Cartella
            packageName      // Il package della tua app
        )
        // 3. Impostiamo l'immagine
        val imgDetail = findViewById<ImageView>(R.id.imgCarDetail) // L'ImageView che abbiamo aggiunto prima

        if (resourceId != 0) {
            // Trovata!
            imgDetail.setImageResource(resourceId)
        } else {
            // Non trovata: mostriamo un placeholder generico o nascondiamo
            imgDetail.setImageResource(R.drawable.car_placeholder)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext, this)
            carVersions = db.carDao().getVersionsByFamily(familyName)

            withContext(Dispatchers.Main) {
                if (carVersions.isNotEmpty()) {
                    txtBrand.text = carVersions[0].brand.uppercase()
                    setupSpinner()
                } else {
                    Toast.makeText(this@CarDetailActivity, "Errore caricamento dati", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 1. Setup Bottone "Cerca Altro"
        findViewById<Button>(R.id.btnNavSearch).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
            // Opzionale: finish() se vuoi chiudere questa scheda
        }
        findViewById<Button>(R.id.btnNavHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // FLAG IMPORTANTE: Questo dice ad Android "Torna alla Home esistente
            // e chiudi tutte le finestre che c'erano sopra (Dettagli, Ricerca, ecc)"
            // Così se premi "Indietro" dalla Home, l'app si chiude invece di tornare qui.
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnNavCompare).setOnClickListener {
            // 1. Controlliamo se la lista è caricata
            if (::carVersions.isInitialized && carVersions.isNotEmpty()) {

                // 2. Recuperiamo la posizione selezionata nello Spinner
                val selectedIndex = spinner.selectedItemPosition

                // 3. Prendiamo l'oggetto CarEntity corrispondente
                val currentCar = carVersions[selectedIndex]

                val intent = Intent(this, SearchActivity::class.java)

                // Passiamo la famiglia ("Fiat Panda")
                intent.putExtra("COMPARE_SOURCE", currentCar.modelFamily)

                // --- MODIFICA FONDAMENTALE: Passiamo anche il TRIM ("1.2 Lounge") ---
                intent.putExtra("COMPARE_SOURCE_TRIM", currentCar.trim)

                startActivity(intent)
            }
        }
    }

    private fun setupSpinner() {
        val labels = carVersions.map { "${it.trim} (${it.year})" }
        val adapter = ArrayAdapter(this, R.layout.spinner_item, labels)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateUI(carVersions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateUI(car: CarEntity) {
        // Helper per settare icona, valore e etichetta velocemente
        // Nota: Assicurati che i nomi delle icone (R.drawable.ic_...) esistano!

        // --- PRESTAZIONI ---
        // Passiamo il valore (Any? perché può essere Int o Double) e l'unità di misura
        setSpec(R.id.specPower, R.drawable.engine, car.power, "CV", "Potenza")
        setSpec(R.id.specTorque, R.drawable.torque, car.torque, "Nm", "Coppia")
        setSpec(R.id.specAccel, R.drawable.acceleration, car.acceleration, "s", "0-100 km/h") // Double
        setSpec(R.id.specSpeed, R.drawable.max_speed, car.topSpeed, "km/h", "Vel. Max")

        // --- MOTORE ---
        setSpec(R.id.specCapacity, R.drawable.capacity, car.engineCapacity, "cc", "Cilindrata")
        setSpec(R.id.specCylinders, R.drawable.cylinders, car.cylinders, "", "Cilindri") // Niente unità qui
        setSpec(R.id.specTurbo, R.drawable.turbo, car.turbo, "", "Turbo") // È una stringa ("Yes"/"No")
        setSpec(R.id.specFuel, R.drawable.fuel, car.fuel, "", "Carburante") // È una stringa

        // --- TELAIO ---
        setSpec(R.id.specDrive, R.drawable.traction, car.driveWheels, "", "Trazione")
        setSpec(R.id.specTrans, R.drawable.gearbox, car.transmission, "", "Cambio")
        setSpec(R.id.specWeight, R.drawable.weight, car.weight, "kg", "Peso")
        setSpec(R.id.specLength, R.drawable.length, car.length, "mm", "Lunghezza")
    }

    // Funzione magica per riempire i piccoli layout "include"
    private fun setSpec(includeId: Int, iconId: Int, value: Any?, unit: String, label: String) {
        val view = findViewById<View>(includeId)
        val img = view.findViewById<ImageView>(R.id.imgIcon)
        val txtVal = view.findViewById<TextView>(R.id.txtValue)
        val txtLab = view.findViewById<TextView>(R.id.txtLabel)

        img.setImageResource(iconId)
        txtLab.text = label

        // Logica di formattazione
        if (value != null) {
            // Se l'unità non è vuota, la aggiunge con uno spazio
            val unitString = if (unit.isNotEmpty()) " $unit" else ""
            txtVal.text = "$value$unitString"
        } else {
            txtVal.text = "-"
        }
    }
}