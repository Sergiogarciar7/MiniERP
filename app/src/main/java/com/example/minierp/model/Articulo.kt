package com.example.minierp.model

data class Articulo(
    var nombre:String?=null,
    var descripcion: String?=null,
    var precio: Float? =null,
    var stock:Int?=null
):java.io.Serializable