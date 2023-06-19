package com.example.minierp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.model.Facturaventa

class FacturaventaAdapter(
    var lista: List<Facturaventa>,
    private val onItemBorrar: (Int) -> Unit,
    private val onItemEdit: (Facturaventa) -> Unit,
    private val onItemImprimir: (Facturaventa) -> Unit
) : RecyclerView.Adapter<FacturasventaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturasventaViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.facturasventas_layout, parent, false)
        return FacturasventaViewHolder(v)
    }

    override fun onBindViewHolder(holder: FacturasventaViewHolder, position: Int) {
        val facturaventa = lista[position]
        holder.render(facturaventa, onItemBorrar, onItemEdit,onItemImprimir)
    }

    override fun getItemCount() = lista.size
}
