package com.example.carspotter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CarDao {
    // --- QUERY DATASET TECNICO ---

    @Query("SELECT * FROM cars WHERE model_family = :familyName ORDER BY year DESC")
    suspend fun getVersionsByFamily(familyName: String): List<CarEntity>

    @Query("SELECT * FROM cars WHERE model_family LIKE '%' || :query || '%' GROUP BY model_family ORDER BY brand, model_family")
    suspend fun searchUniqueFamilies(query: String): List<CarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cars: List<CarEntity>)

    @Query("SELECT COUNT(*) FROM cars")
    suspend fun getCount(): Int

    // --- QUERY GARAGE (CRONOLOGIA) ---

    @Insert
    suspend fun addToGarage(item: ScannedCar)

    @Query("SELECT * FROM garage ORDER BY timestamp DESC")
    suspend fun getGarageItems(): List<ScannedCar>

    @Query("DELETE FROM garage")
    suspend fun clearGarage()
}