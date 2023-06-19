package com.example.minierp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.model.Articulo
import com.example.minierp.model.Cliente
import com.example.minierp.model.Proveedor

class ProveedorAdapter(
    var lista: List<Proveedor>,
    private val onItemBorrar: (Int) -> Unit,
    private var onItemEdit: (Proveedor) -> Unit
) : RecyclerView.Adapter<ProveedorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProveedorViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.proveedores_layout, parent, false)
        return ProveedorViewHolder(v)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ProveedorViewHolder, position: Int) {
        val proveedor = lista[position]
        holder.render(proveedor, onItemBorrar, onItemEdit)
    }
}
