package com.example.minierp.model

import java.text.SimpleDateFormat
import java.util.*

data class Facturaventa(
    var nombrearticulo:String?=null,
    var id: String?=null,
    var importe: Float? =null,
    var cantidad:Int?=null,
    var fecha: String? = null,
    var cliente: String? = null
) : java.io.Serializable {

    init {
        // Obtener la fecha actual
        val currentDate = Calendar.getInstance().time

        // Formatear la fecha en el formato deseado
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        fecha = dateFormat.format(currentDate)
    }
}