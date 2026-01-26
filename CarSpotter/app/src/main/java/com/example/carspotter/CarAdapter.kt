package com.example.carspotter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter per visualizzare i risultati della ricerca dei modelli auto.
 */
class CarAdapter(
    private var cars: List<CarEntity>,
    private val onClick: (CarEntity) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtBrand: TextView = view.findViewById(R.id.txtBrandCustom)
        val txtModel: TextView = view.findViewById(R.id.txtModelCustom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_search, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.txtBrand.text = car.brand
        holder.txtModel.text = car.modelFamily
        holder.itemView.setOnClickListener { onClick(car) }
    }

    override fun getItemCount() = cars.size

    fun updateList(newList: List<CarEntity>) {
        cars = newList
        notifyDataSetChanged()
    }
}