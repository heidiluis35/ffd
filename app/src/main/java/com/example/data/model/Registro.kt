package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "registros")
data class Registro(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val alumnoUid: String = "",
    val numeroControl: String = "",
    val nombre: String = "",
    val carrera: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tipo: String = "entrada", // SOLO ENTRADA
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distanciaMetros: Double = 0.0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "alumnoUid" to alumnoUid,
            "numeroControl" to numeroControl,
            "nombre" to nombre,
            "carrera" to carrera,
            "timestamp" to timestamp,
            "tipo" to tipo,
            "latitude" to latitude,
            "longitude" to longitude,
            "distanciaMetros" to distanciaMetros
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Registro {
            return Registro(
                id = id,
                alumnoUid = map["alumnoUid"] as? String ?: "",
                numeroControl = map["numeroControl"] as? String ?: "",
                nombre = map["nombre"] as? String ?: "",
                carrera = map["carrera"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                tipo = "entrada",
                latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
                distanciaMetros = (map["distanciaMetros"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}
