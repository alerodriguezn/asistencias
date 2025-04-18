package com.example.asistencias.data

data class Jornada(
    val id: String = "",
    val nombre: String = "",
    val anio: Int = 0,
    val semestre: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val estado: String = ""
) {
    constructor() : this("", "", 0, "", "", "", "")
}
