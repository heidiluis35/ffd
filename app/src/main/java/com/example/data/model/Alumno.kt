package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alumnos")
data class Alumno(
    @PrimaryKey
    val uid: String = "", // NFC Tag UID in Hex format (e.g., "04A1B2C3")
    val numeroControl: String = "",
    val nombre: String = "",
    val carrera: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()
) {
    // No-arg constructor map conversion for Firebase Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "numeroControl" to numeroControl,
            "nombre" to nombre,
            "carrera" to carrera,
            "fechaRegistro" to fechaRegistro
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Alumno {
            return Alumno(
                uid = map["uid"] as? String ?: "",
                numeroControl = map["numeroControl"] as? String ?: "",
                nombre = map["nombre"] as? String ?: "",
                carrera = map["carrera"] as? String ?: "",
                fechaRegistro = (map["fechaRegistro"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
