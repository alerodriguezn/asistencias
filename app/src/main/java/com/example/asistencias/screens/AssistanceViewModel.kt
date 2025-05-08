package com.example.asistencias.screens

import androidx.lifecycle.ViewModel
import com.example.asistencias.data.Assistance
import com.google.firebase.firestore.FirebaseFirestore

class AssistanceViewModel: ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val collectionPath = "assistances"

    var editingItemId: String? = null


    fun saveAssistance(assistance: Assistance, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (editingItemId == null) {
            val newDocument = db.collection(collectionPath).document()
            val assistanceWithId = assistance.copy(id = newDocument.id)

            newDocument.set(assistanceWithId)
                .addOnSuccessListener {
                    editingItemId = newDocument.id
                    onSuccess()
                }
                .addOnFailureListener { onError(it) }

        } else {
            val assistanceWithId = assistance.copy(id = editingItemId!!)
            db.collection(collectionPath)
                .document(editingItemId!!)
                .set(assistanceWithId)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it) }
        }
    }

    fun deleteAssistance(assistanceId: String, onComplete: () -> Unit) {
        db.collection(collectionPath)
            .document(assistanceId)
            .delete()
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    fun getAssistanceById(assistanceId: String, onResult: (Assistance?) -> Unit) {
        db.collection(collectionPath)
            .document(assistanceId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val assistance = document.toObject(Assistance::class.java)
                    onResult(assistance)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }



}