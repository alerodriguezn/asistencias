package com.example.asistencias.data

data class User(
    val nombre: String = "",
    val apellido1: String = "",
    val apellido2: String = "",
    val cedula: String = "",
    val correo: String = "",
    val carnet: String = "",
    val carrera: String = "",
    val rol: String = ""
) {
    constructor() : this("", "", "", "", "", "", "", "")
} 