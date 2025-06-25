package com.example.asistencias.data

import com.google.firebase.Timestamp

data class StudentApplication(
    val id: String = "",
    val assistanceRequestId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val studentEmail: String = "",
    val studentCarnet: String = "",
    val studentCarrera: String = "",
    val status: String = "Pendiente", // Pendiente, Aprobada, Rechazada
    val applicationDate: Timestamp = Timestamp.now(),
    val reviewDate: Timestamp? = null,
    val reviewerId: String? = null,
    val reviewerName: String? = null,
    val comments: String = "",
    val motivation: String = "", // Motivación del estudiante para aplicar
    val bankAccount: String = "", // Cuenta bancaria del estudiante
    val weightedAverage: String = "" // Promedio ponderado del estudiante
) {
    constructor() : this("", "", "", "", "", "", "", "Pendiente", Timestamp.now(), null, null, null, "", "", "", "")
} 