package com.example.minierp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.model.Articulo
import com.example.minierp.model.Facturacompra
import com.example.minierp.model.Facturaventa

class FacturacompraAdapter(var lista: List<Facturacompra>,
                           private val onItemBorrar: (Int) -> Unit,
                           private var onItemEdit: (Facturacompra) ->Unit,
                           private val onItemImprimir: (Facturacompra) -> Unit
): RecyclerView.Adapter<FacturascompraViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturascompraViewHolder {
        val v= LayoutInflater.from(parent.context).inflate(R.layout.facturascompras_layout,parent,false)
        return FacturascompraViewHolder(v)
    }

    override fun onBindViewHolder(holder: FacturascompraViewHolder, position: Int) {
        val facturacompra=lista[position]
        holder.render(facturacompra,onItemBorrar,onItemEdit,onItemImprimir)
    }

    override fun getItemCount() = lista.size

}