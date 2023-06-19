package com.example.minierp.adapter

import android.app.AlertDialog
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.databinding.ArticulosLayoutBinding
import com.example.minierp.databinding.ClienteLayoutBinding
import com.example.minierp.databinding.ProveedoresLayoutBinding
import com.example.minierp.model.Articulo
import com.example.minierp.model.Cliente
import com.example.minierp.model.Proveedor


class ProveedorViewHolder (v: View): RecyclerView.ViewHolder(v) {

    private val binding = ProveedoresLayoutBinding.bind(v)

    fun render(proveedor: Proveedor, onItemBorrar: (Int) -> Unit, onItemEdit: (Proveedor) -> Unit) {
        binding.tvNombrepro.text = proveedor.nombrepro
        binding.tvDireccionpro.text = proveedor.direcionpro
        binding.tvNifpro.text = proveedor.nifpro
        binding.tvIdpro.text = proveedor.Idpro

        binding.btnDelete.setOnClickListener {
            mostrarModalBorrar(adapterPosition, onItemBorrar)
        }

        binding.btnEditar.setOnClickListener {
            onItemEdit(proveedor)
        }
    }

    private fun mostrarModalBorrar(position: Int, onItemBorrar: (Int) -> Unit) {
        val alertDialog = AlertDialog.Builder(itemView.context)
            .setTitle("Confirmar borrado")
            .setMessage("¿Estás seguro de que deseas borrar este proveedor?")
            .setPositiveButton("Borrar") { _, _ ->
                onItemBorrar(position)
            }
            .setNegativeButton("Cancelar", null)
            .create()
        alertDialog.show()
    }
}
