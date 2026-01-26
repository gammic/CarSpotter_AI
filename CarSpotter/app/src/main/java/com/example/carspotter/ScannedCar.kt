package com.example.carspotter

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Rappresenta un veicolo salvato nel "Garage" (cronologia avvistamenti).
 */
@Entity(tableName = "garage")
data class ScannedCar(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "model_family") val modelFamily: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "image_path") val imagePath: String // Percorso locale della foto scattata
)