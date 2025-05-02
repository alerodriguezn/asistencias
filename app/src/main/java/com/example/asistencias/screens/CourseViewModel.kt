package com.example.asistencias.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.asistencias.data.Course
import com.google.firebase.firestore.FirebaseFirestore

class CourseViewModel: ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val collectionPath = "courses"

    var editingItemId: String? = null


    fun saveCourse(course: Course, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (editingItemId == null) {
            val newDocument = db.collection(collectionPath).document()
            val courseWithId = course.copy(id = newDocument.id)
            newDocument.set(courseWithId)
                .addOnSuccessListener {
                    editingItemId = newDocument.id
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    Log.e("CourseViewModel", "Error saving course: ", exception)
                    onError(exception)
                }

        } else {
            val courseWithId = course.copy(id = editingItemId!!)
            db.collection(collectionPath)
                .document(editingItemId!!)
                .set(courseWithId)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it) }
        }
    }

    fun deleteCourse(courseId: String, onComplete: () -> Unit) {
        db.collection(collectionPath)
            .document(courseId)
            .delete()
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    fun getCourseById(courseId: String, onResult: (Course?) -> Unit) {
        db.collection(collectionPath)
            .document(courseId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val course = document.toObject(Course::class.java)
                    onResult(course)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }



}