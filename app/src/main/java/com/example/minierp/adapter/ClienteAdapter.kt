package com.example.minierp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.model.Articulo
import com.example.minierp.model.Cliente

class ClienteAdapter(var lista: List<Cliente>,
                     private val onItemBorrar: (Int) -> Unit,
                     private var onItemEdit: (Cliente) ->Unit
): RecyclerView.Adapter<ClienteViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val v= LayoutInflater.from(parent.context).inflate(R.layout.cliente_layout,parent,false)
        return ClienteViewHolder(v)
    }



    override fun getItemCount() = lista.size
    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente =lista[position]
        holder.render(cliente,onItemBorrar,onItemEdit)
    }
}