package com.example.asistencias.data

data class Assistance(
    val id: String = "",
    val name: String = "",
    val description: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "")
}