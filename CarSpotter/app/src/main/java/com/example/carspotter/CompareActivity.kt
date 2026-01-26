package com.example.carspotter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.ContextCompat

/**
 * Gestisce il confronto tecnico tra due veicoli selezionati.
 */
class CompareActivity : AppCompatActivity() {

    private val HIGHER_BETTER = 1
    private val LOWER_BETTER = 2
    private val NEUTRAL = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)

        val familyA = intent.getStringExtra("CAR_A") ?: return
        val familyB = intent.getStringExtra("CAR_B") ?: return
        val trimA = intent.getStringExtra("TRIM_A")
        val trimB = intent.getStringExtra("TRIM_B")

        setupNavigation()
        loadComparisonData(familyA, trimA, familyB, trimB)
    }

    private fun setupNavigation() {
        findViewById<Button>(R.id.btnCompHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnCompNewSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun loadComparisonData(famA: String, trimA: String?, famB: String, trimB: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext, this)

            val versionsA = db.carDao().getVersionsByFamily(famA)
            val versionsB = db.carDao().getVersionsByFamily(famB)

            val carA = versionsA.find { it.trim == trimA } ?: versionsA.firstOrNull()
            val carB = versionsB.find { it.trim == trimB } ?: versionsB.firstOrNull()

            withContext(Dispatchers.Main) {
                if (carA != null && carB != null) {
                    displayCars(carA, carB)
                }
            }
        }
    }

    private fun displayCars(carA: CarEntity, carB: CarEntity) {
        findViewById<TextView>(R.id.txtCarNameA).text = "${carA.modelFamily}\n${carA.trim}"
        findViewById<TextView>(R.id.txtCarNameB).text = "${carB.modelFamily}\n${carB.trim}"

        // Prestazioni
        setupRow(R.id.rowPower, "Potenza", carA.power, carB.power, "CV", HIGHER_BETTER)
        setupRow(R.id.rowTorque, "Coppia", carA.torque, carB.torque, "Nm", HIGHER_BETTER)
        setupRow(R.id.rowSpeed, "Vel. Max", carA.topSpeed, carB.topSpeed, "km/h", HIGHER_BETTER)
        setupRow(R.id.rowAccel, "0-100", carA.acceleration, carB.acceleration, "s", LOWER_BETTER)

        // Motore e Dimensioni
        setupRow(R.id.rowCapacity, "Cilindrata", carA.engineCapacity, carB.engineCapacity, "cc", HIGHER_BETTER)
        setupRow(R.id.rowWeight, "Peso", carA.weight, carB.weight, "kg", LOWER_BETTER)
        setupRow(R.id.rowLength, "Lunghezza", carA.length, carB.length, "mm", LOWER_BETTER)

        // Dati testuali
        setupRow(R.id.rowFuel, "Carburante", carA.fuel, carB.fuel, "", NEUTRAL)
        setupRow(R.id.rowTrans, "Cambio", carA.transmission, carB.transmission, "", NEUTRAL)
    }

    private fun setupRow(viewId: Int, label: String, valA: Any?, valB: Any?, unit: String, mode: Int) {
        val view = findViewById<View>(viewId)
        val txtA = view.findViewById<TextView>(R.id.txtValueA)
        val txtB = view.findViewById<TextView>(R.id.txtValueB)
        val txtLabel = view.findViewById<TextView>(R.id.txtLabel)

        txtLabel.text = label
        txtA.text = if (valA != null) "$valA $unit".trim() else "-"
        txtB.text = if (valB != null) "$valB $unit".trim() else "-"

        val colorWin = 0xFF2E7D32.toInt()
        val colorLose = 0xFFC62828.toInt()
        val colorNeutral = ContextCompat.getColor(this, R.color.my_text_primary)

        if (mode == NEUTRAL || valA == null || valB == null) return

        try {
            val numA = valA.toString().toDouble()
            val numB = valB.toString().toDouble()

            if (numA != numB) {
                val aWins = if (mode == HIGHER_BETTER) numA > numB else numA < numB
                txtA.setTextColor(if (aWins) colorWin else colorLose)
                txtB.setTextColor(if (aWins) colorLose else colorWin)
            }
        } catch (e: Exception) {
            txtA.setTextColor(colorNeutral)
            txtB.setTextColor(colorNeutral)
        }
    }
}