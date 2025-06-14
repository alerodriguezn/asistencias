package com.example.asistencias.data

data class Comunicado(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fechaPublicacion: String = "",  // O Long si usás timestamp
    val fechaCierre: String = "",
    val estado: String = "activo",
    val autor: String = ""
)