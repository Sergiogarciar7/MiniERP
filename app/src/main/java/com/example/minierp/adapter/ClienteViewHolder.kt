package com.example.minierp.adapter

import android.app.AlertDialog
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.databinding.ArticulosLayoutBinding
import com.example.minierp.databinding.ClienteLayoutBinding
import com.example.minierp.model.Articulo
import com.example.minierp.model.Cliente


class ClienteViewHolder (v: View): RecyclerView.ViewHolder(v) {

    private val binding= ClienteLayoutBinding.bind(v)

    fun render(cliente: Cliente, onItemBorrar: (Int) -> Unit, onItemEdit: (Cliente) -> Unit)
    {
        binding.tvNombrecli.text = cliente.nombrecli
        binding.tvDireccion.text = cliente.direcion
        binding.tvNifcli.text = cliente.nif
        binding.tvIdcli.text =  cliente.Idcli

        binding.btnDelete.setOnClickListener {
            //onItemBorrar(adapterPosition)
            mostrarModalBorrar(adapterPosition, onItemBorrar)
        }

        binding.btnEditar.setOnClickListener {
            onItemEdit(cliente)
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
