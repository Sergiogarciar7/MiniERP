package com.example.minierp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.model.Articulo

class ArticuloAdapter(var lista: List<Articulo>,
                      private val onItemBorrar: (Int) -> Unit,
                      private var onItemEdit: (Articulo) ->Unit
): RecyclerView.Adapter<ArticulosViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticulosViewHolder {
        val v= LayoutInflater.from(parent.context).inflate(R.layout.articulos_layout,parent,false)
        return ArticulosViewHolder(v)
    }

    override fun onBindViewHolder(holder: ArticulosViewHolder, position: Int) {
        val articulo =lista[position]
        holder.render(articulo,onItemBorrar,onItemEdit)
    }

    override fun getItemCount() = lista.size
}