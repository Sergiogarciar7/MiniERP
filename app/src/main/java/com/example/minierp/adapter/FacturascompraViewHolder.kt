package com.example.minierp.adapter

import android.app.AlertDialog
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.minierp.R
import com.example.minierp.databinding.ArticulosLayoutBinding
import com.example.minierp.databinding.FacturascomprasLayoutBinding
import com.example.minierp.model.Articulo
import com.example.minierp.model.Facturacompra
import com.example.minierp.model.Facturaventa


class FacturascompraViewHolder (v: View): RecyclerView.ViewHolder(v) {

    private val binding= FacturascomprasLayoutBinding.bind(v)

    fun render(facturacompra: Facturacompra, onItemBorrar: (Int) -> Unit, onItemEdit: (Facturacompra) -> Unit,  onItemImprimir: (Facturacompra) -> Unit) {
        binding.tvNombreartfactcom.text = facturacompra.nombrearticulo
        binding.tvIdfactcom.text = facturacompra.id
        binding.tvImportetcom.text = String.format(binding.tvImportetcom.context.getString(R.string.Importe),facturacompra.importe)
        binding.tvCantidadtcom.text =  String.format(binding.tvCantidadtcom.context.getString(R.string.Cantidad),facturacompra.cantidad)
        binding.tvFechafactcom.text = facturacompra.fecha
        binding.tvNombrecprov.text =facturacompra.proveedor

        binding.btnDelete.setOnClickListener {
            //onItemBorrar(adapterPosition)
            mostrarModalBorrar(adapterPosition, onItemBorrar)
        }

        binding.btnEditar.setOnClickListener {
            onItemEdit(facturacompra)
        }
        binding.btnImprimir3.setOnClickListener {
            mostrarModalImprimir(facturacompra, onItemImprimir)
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
    private fun mostrarModalImprimir(facturacompra: Facturacompra, onItemImprimir: (Facturacompra) -> Unit) {
        val alertDialog = AlertDialog.Builder(itemView.context)
            .setTitle("Confirmar impresión")
            .setMessage("¿Estás seguro de que deseas imprimir esta factura?")
            .setPositiveButton("Imprimir") { _, _ ->
                onItemImprimir(facturacompra)
            }
            .setNegativeButton("Cancelar", null)
            .create()
        alertDialog.show()
    }
}
