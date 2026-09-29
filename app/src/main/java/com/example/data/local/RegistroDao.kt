package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Registro
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {
    @Query("SELECT * FROM registros ORDER BY timestamp DESC")
    fun getAllRegistros(): Flow<List<Registro>>

    @Query("SELECT * FROM registros WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getRegistrosToday(startOfDay: Long): Flow<List<Registro>>

    @Query("SELECT * FROM registros WHERE alumnoUid = :alumnoUid AND timestamp >= :startOfDay ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRegistroToday(alumnoUid: String, startOfDay: Long): Registro?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistro(registro: Registro)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(registros: List<Registro>)

    @Query("DELETE FROM registros")
    suspend fun clearAll()
}
