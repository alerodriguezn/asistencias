package com.example.asistencias.data

data class Assistance(
    val id: String = "",
    val name: String = "",
    val requirements: String = "",
    val benefits: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "")
}