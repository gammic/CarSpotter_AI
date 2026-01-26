package com.example.carspotter

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException

/**
 * Wrapper per il modello TensorFlow Lite che riconosce i modelli di auto.
 */
class CarClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    // Requisiti standard MobileNetV2
    private val INPUT_SIZE = 224
    private val NORM_MEAN = 127.5f
    private val NORM_STD = 127.5f

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            val modelFile = FileUtil.loadMappedFile(context, "car_recognizer.tflite")
            interpreter = Interpreter(modelFile, Interpreter.Options())
            labels = FileUtil.loadLabels(context, "labels.txt")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Elabora l'immagine e restituisce il nome del modello e la confidenza.
     * @return Stringa formattata "Modello|Accuratezza%"
     */
    fun classify(bitmap: Bitmap): String {
        if (interpreter == null) return "Errore Modello"

        // Pre-processing: ridimensionamento e normalizzazione (-1 a 1)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(NORM_MEAN, NORM_STD))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Output: Array di probabilità per ogni label
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, labels.size), DataType.FLOAT32)
        interpreter?.run(tensorImage.buffer, outputBuffer.buffer.rewind())

        // Ricerca del risultato migliore
        val probabilities = outputBuffer.floatArray
        var maxIndex = 0
        var maxProb = 0f

        for (i in probabilities.indices) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIndex = i
            }
        }

        val carName = if (maxIndex < labels.size) labels[maxIndex] else "Sconosciuta"
        return "$carName|${"%.1f".format(maxProb * 100)}%"
    }
}