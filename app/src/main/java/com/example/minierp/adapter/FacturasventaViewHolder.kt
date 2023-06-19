package com.example.minierp.adapter

import android.app.AlertDialog
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.databinding.FacturascomprasLayoutBinding
import com.example.minierp.databinding.FacturasventasLayoutBinding
import com.example.minierp.model.Facturacompra
import com.example.minierp.model.Facturaventa


class FacturasventaViewHolder (v: View): RecyclerView.ViewHolder(v) {

    private val binding = FacturasventasLayoutBinding.bind(v)

    fun render(
        facturaventa: Facturaventa,
        onItemBorrar: (Int) -> Unit,
        onItemEdit: (Facturaventa) -> Unit,
        onItemImprimir: (Facturaventa) -> Unit
    ) {
        binding.tvNombreartfac.text = facturaventa.nombrearticulo
        binding.tvNombrecliente.text = facturaventa.cliente
        binding.tvIdfacv.text = facturaventa.id
        binding.tvImporte.text = String.format(
            binding.tvImporte.context.getString(R.string.Importe),
            facturaventa.importe
        )
        binding.tvCantidad.text = String.format(
            binding.tvCantidad.context.getString(R.string.Cantidad),
            facturaventa.cantidad
        )
        binding.tvFechafac.text = facturaventa.fecha

        binding.btnDelete.setOnClickListener {
            mostrarModalBorrar(adapterPosition, onItemBorrar)
        }

        binding.btnEditar.setOnClickListener {
            onItemEdit(facturaventa)
        }
        binding.btnImprimir2.setOnClickListener {
            mostrarModalImprimir(facturaventa, onItemImprimir)
        }
    }

    private fun mostrarModalBorrar(position: Int, onItemBorrar: (Int) -> Unit) {
        val alertDialog = AlertDialog.Builder(itemView.context)
            .setTitle("Confirmar borrado")
            .setMessage("¿Estás seguro de que deseas borrar esta factura?")
            .setPositiveButton("Borrar") { _, _ ->
                onItemBorrar(position)
            }
            .setNegativeButton("Cancelar", null)
            .create()
        alertDialog.show()
    }
    private fun mostrarModalImprimir(facturaventa: Facturaventa, onItemImprimir: (Facturaventa) -> Unit) {
        val alertDialog = AlertDialog.Builder(itemView.context)
            .setTitle("Confirmar impresión")
            .setMessage("¿Estás seguro de que deseas imprimir esta factura?")
            .setPositiveButton("Imprimir") { _, _ ->
                onItemImprimir(facturaventa)
            }
            .setNegativeButton("Cancelar", null)
            .create()
        alertDialog.show()
    }
}
