package com.example.carspotter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter per la lista del Garage. Gestisce il caricamento di foto scattate dall'utente
 * o immagini di fallback.
 */
class GarageAdapter(
    private val items: List<ScannedCar>,
    private val onClick: (ScannedCar) -> Unit
) : RecyclerView.Adapter<GarageAdapter.GarageViewHolder>() {

    class GarageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtGarageName)
        val txtDate: TextView = view.findViewById(R.id.txtGarageDate)
        val imgCar: ImageView = view.findViewById(R.id.imgGarageCar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GarageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_garage, parent, false)
        return GarageViewHolder(view)
    }

    override fun onBindViewHolder(holder: GarageViewHolder, position: Int) {
        val item = items[position]
        holder.txtName.text = item.modelFamily

        val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
        holder.txtDate.text = "Avvistata: ${sdf.format(Date(item.timestamp))}"

        if (!loadUserPhoto(holder.imgCar, item.imagePath)) {
            loadPlaceholder(holder.imgCar, item.modelFamily)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    private fun loadUserPhoto(imageView: ImageView, path: String): Boolean {
        if (path.isEmpty()) return false
        return try {
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                true
            } else false
        } catch (e: Exception) { false }
    }

    private fun loadPlaceholder(imageView: ImageView, modelFamily: String) {
        val context = imageView.context
        val imageName = modelFamily.lowercase().replace(" ", "_").replace("-", "_")
        val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        imageView.setImageResource(if (resId != 0) resId else R.drawable.car_placeholder)
    }

    override fun getItemCount() = items.size
}