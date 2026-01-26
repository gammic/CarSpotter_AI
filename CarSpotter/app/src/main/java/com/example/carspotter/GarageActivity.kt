package com.example.carspotter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GarageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garage) // Crea questo XML con solo una RecyclerView

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerGarage)
        val emptyView = findViewById<View>(R.id.emptyView)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish() // Chiude l'activity e torna alla Home
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext, this)
            val history = db.carDao().getGarageItems()

            withContext(Dispatchers.Main) {
                if (history.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    recyclerView.adapter = GarageAdapter(history) { scannedCar ->
                        // Al click riapriamo la scheda tecnica!
                        val intent = Intent(this@GarageActivity, CarDetailActivity::class.java)
                        intent.putExtra("CAR_FAMILY", scannedCar.modelFamily)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}