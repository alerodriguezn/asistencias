package com.example.asistencias.data

import com.google.firebase.Timestamp

data class AssistanceRequest(
    val id: String = "",
    val jornadaId: String = "",
    val jornadaNombre: String = "",
    val assistanceTypeId: String = "",
    val assistanceTypeName: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val professorId: String = "",
    val professorName: String = "",
    val status: String = "Pendiente", // Pendiente, Aprobada, Rechazada
    val requestDate: Timestamp = Timestamp.now(),
    val reviewDate: Timestamp? = null,
    val reviewerId: String? = null,
    val reviewerName: String? = null,
    val comments: String = "",
    val createdBy: String = "",
    val createdByName: String = ""
) {
    constructor() : this("", "", "", "", "", "", "", "", "", "Pendiente", Timestamp.now(), null, null, null, "", "", "")
} 