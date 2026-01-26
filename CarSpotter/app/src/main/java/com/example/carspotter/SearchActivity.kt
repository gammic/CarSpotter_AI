package com.example.carspotter

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var adapter: CarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val etSearch = findViewById<EditText>(R.id.etSearch)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerCars)

        val sourceCarFamily = intent.getStringExtra("COMPARE_SOURCE")
        val sourceCarTrim = intent.getStringExtra("COMPARE_SOURCE_TRIM")

        // Setup RecyclerView
        adapter = CarAdapter(emptyList()) { selectedCar ->
            if (sourceCarFamily != null) {
                // --- MODALITÀ CONFRONTO ---
                showVersionSelector(sourceCarFamily, sourceCarTrim, selectedCar.modelFamily)
            } else {
                // --- MODALITÀ NORMALE ---
                val intent = Intent(this, CarDetailActivity::class.java)
                intent.putExtra("CAR_FAMILY", selectedCar.modelFamily)
                startActivity(intent)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup Ricerca in tempo reale
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                cercaAuto(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Carica tutto all'inizio
        cercaAuto("")
    }

    private fun showVersionSelector(sourceFamily: String, sourceTrim: String?, targetFamily: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext, this)

            // 1. Scarichiamo tutte le versioni dell'auto cliccata (Target)
            val versions = db.carDao().getVersionsByFamily(targetFamily)

            withContext(Dispatchers.Main) {
                if (versions.isEmpty()) return@withContext

                // Se c'è solo una versione, non chiediamo nulla e andiamo dritti
                if (versions.size == 1) {
                    launchCompare(sourceFamily, sourceTrim, targetFamily, versions[0].trim)
                } else {
                    // 2. Creiamo la lista di stringhe per il Dialog
                    // Mostriamo: "1.2 Lounge (2012)"
                    val items = versions.map { "${it.trim} (${it.year})" }.toTypedArray()

                    // 3. Mostriamo il Dialog
                    AlertDialog.Builder(this@SearchActivity)
                        .setTitle("Scegli versione di $targetFamily")
                        .setItems(items) { _, which ->
                            // 'which' è l'indice cliccato (0, 1, 2...)
                            val selectedTrim = versions[which].trim
                            launchCompare(sourceFamily, sourceTrim, targetFamily, selectedTrim)
                        }
                        .show()
                }
            }
        }
    }

    private fun launchCompare(familyA: String, trimA: String?, familyB: String, trimB: String) {
        val intent = Intent(this, CompareActivity::class.java)
        intent.putExtra("CAR_A", familyA)
        intent.putExtra("CAR_B", familyB)

        intent.putExtra("TRIM_A", trimA)
        intent.putExtra("TRIM_B", trimB)

        startActivity(intent)
        finish() // Chiude la ricerca
    }

    private fun cercaAuto(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext, this)
            // Se la query è vuota, mostriamo tutto (o niente, come preferisci)
            // Attenzione: se hai 80k auto, meglio mostrare solo se query.length > 2

            val results = if (query.length > 1) {
                db.carDao().searchUniqueFamilies(query)
            } else {
                emptyList() // Non mostrare nulla se non scrivi
            }

            withContext(Dispatchers.Main) {
                adapter.updateList(results)
            }
        }
    }
}