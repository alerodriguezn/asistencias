package com.example.asistencias.data


data class Course (
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val professorIds: List<String> = emptyList()
) {

    constructor() : this("", "", "", emptyList())
}