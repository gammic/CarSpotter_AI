package com.example.carspotter

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Rappresenta una specifica versione di un veicolo nel database tecnico.
 */
@Entity(tableName = "cars")
data class CarEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "brand") val brand: String,
    @ColumnInfo(name = "model_family") val modelFamily: String, // Nome usato per il matching AI
    @ColumnInfo(name = "trim") val trim: String,               // Allestimento specifico
    @ColumnInfo(name = "year") val year: Int?,
    @ColumnInfo(name = "model") val completeName: String,

    // Specifiche tecniche
    @ColumnInfo(name = "power") val power: Int?,
    @ColumnInfo(name = "top_speed") val topSpeed: Int?,
    @ColumnInfo(name = "acc_0_100") val acceleration: Double?,
    @ColumnInfo(name = "fuel") val fuel: String?,
    @ColumnInfo(name = "cylinders") val cylinders: String?,
    @ColumnInfo(name = "transmission") val transmission: String?,
    @ColumnInfo(name = "torque") val torque: Int?,
    @ColumnInfo(name = "turbo") val turbo: String?,
    @ColumnInfo(name = "eng_capacity") val engineCapacity: Int?,
    @ColumnInfo(name = "drive_wheels") val driveWheels: String?,
    @ColumnInfo(name = "weight") val weight: Int?,
    @ColumnInfo(name = "length") val length: Int?
)