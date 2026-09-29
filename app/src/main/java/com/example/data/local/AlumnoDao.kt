package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Alumno
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumnoDao {
    @Query("SELECT * FROM alumnos ORDER BY nombre ASC")
    fun getAllAlumnos(): Flow<List<Alumno>>

    @Query("SELECT * FROM alumnos WHERE uid = :uid LIMIT 1")
    suspend fun getAlumnoByUid(uid: String): Alumno?

    @Query("SELECT * FROM alumnos WHERE numeroControl = :numeroControl LIMIT 1")
    suspend fun getAlumnoByNumeroControl(numeroControl: String): Alumno?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlumno(alumno: Alumno)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alumnos: List<Alumno>)

    @Query("DELETE FROM alumnos WHERE uid = :uid")
    suspend fun deleteAlumno(uid: String)

    @Query("SELECT COUNT(*) FROM alumnos")
    fun getAlumnosCount(): Flow<Int>
}
