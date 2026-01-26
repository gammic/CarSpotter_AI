package com.example.carspotter

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Gestore principale del database Room.
 * Include la logica di popolamento iniziale da file CSV.
 */
@Database(entities = [CarEntity::class, ScannedCar::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope, onProgress: (Int) -> Unit = {}): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_database"
                )
                    .addCallback(CarDatabaseCallback(context, scope, onProgress))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class CarDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope,
        private val onProgress: (Int) -> Unit
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(context, database.carDao())
                }
            }
        }

        /**
         * Legge il dataset CSV dagli assets e lo importa nel database.
         */
        suspend fun populateDatabase(context: Context, carDao: CarDao) {
            if (carDao.getCount() > 0) return

            try {
                // Calcolo righe totali per la barra di caricamento
                val countStream = context.assets.open("cars_final.csv")
                val totalLines = BufferedReader(InputStreamReader(countStream)).useLines { it.count() }
                countStream.close()

                val inputStream = context.assets.open("cars_final.csv")
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.readLine() // Salta intestazione

                val carList = mutableListOf<CarEntity>()
                var line = reader.readLine()
                var currentLine = 0

                while (line != null) {
                    currentLine++
                    val tokens = line.split(",")

                    if (tokens.size >= 17) {
                        try {
                            carList.add(CarEntity(
                                brand = tokens[0].trim(),
                                completeName = tokens[1].trim(),
                                cylinders = tokens[2].trim(),
                                transmission = tokens[3].trim(),
                                power = tokens[4].trim().toIntOrNull(),
                                torque = tokens[5].trim().toIntOrNull(),
                                topSpeed = tokens[6].trim().toIntOrNull(),
                                turbo = tokens[7].trim(),
                                fuel = tokens[8].trim(),
                                acceleration = tokens[9].trim().toDoubleOrNull(),
                                engineCapacity = tokens[10].trim().toIntOrNull(),
                                driveWheels = tokens[11].trim(),
                                weight = tokens[12].trim().toIntOrNull(),
                                length = tokens[13].trim().toIntOrNull(),
                                year = tokens[14].trim().toIntOrNull(),
                                modelFamily = tokens[15].trim(),
                                trim = tokens[16].trim()
                            ))

                            // Notifica progresso ogni 100 righe
                            if (currentLine % 100 == 0) {
                                onProgress((currentLine * 100) / totalLines)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("DB_IMPORT", "Errore riga $currentLine: $line")
                        }
                    }
                    line = reader.readLine()
                }
                carDao.insertAll(carList)
                onProgress(100)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}