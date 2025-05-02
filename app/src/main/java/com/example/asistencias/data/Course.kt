package com.example.asistencias.data


data class Course (
    val id: String = "",
    val code: String = "",
    val name: String = "",
) {

    constructor() : this("", "")
}