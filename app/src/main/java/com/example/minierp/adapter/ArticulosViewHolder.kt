package com.example.minierp.adapter

import android.app.AlertDialog
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.databinding.ArticulosLayoutBinding
import com.example.minierp.model.Articulo


class ArticulosViewHolder (v: View): RecyclerView.ViewHolder(v) {

    private val binding= ArticulosLayoutBinding.bind(v)

    fun render(articulo: Articulo, onItemBorrar: (Int) -> Unit, onItemEdit: (Articulo) -> Unit) {
        binding.tvNombre.text = articulo.nombre
        binding.tvDescripcion.text = articulo.descripcion
        binding.tvPrecio.text = String.format(binding.tvPrecio.context.getString(R.string.precio),articulo.precio)
        binding.tvStock.text =  String.format(binding.tvStock.context.getString(R.string.stock),articulo.stock)

        binding.btnDelete.setOnClickListener {
            //onItemBorrar(adapterPosition)
            mostrarModalBorrar(adapterPosition, onItemBorrar)
        }

        binding.btnEditar.setOnClickListener {
            onItemEdit(articulo)
        }
    }
    private fun mostrarModalBorrar(position: Int, onItemBorrar: (Int) -> Unit) {
        val alertDialog = AlertDialog.Builder(itemView.context)
            .setTitle("Confirmar borrado")
            .setMessage("¿Estás seguro de que deseas borrar este artículo?")
            .setPositiveButton("Borrar") { _, _ ->
                onItemBorrar(position)
            }
            .setNegativeButton("Cancelar", null)
            .create()
        alertDialog.show()
    }
}
