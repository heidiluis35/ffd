package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Alumno
import com.example.data.repository.BibliotecaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bibliotag ITI", appName)
    }

    @Test
    fun `register and record entrance test`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = BibliotecaRepository(context)

        val testAlumno = Alumno(
            uid = "TESTUID123",
            numeroControl = "20089999",
            nombre = "Estudiante de Prueba ITI",
            carrera = "Ing. en Sistemas Computacionales"
        )

        val regResult = repository.registerAlumno(testAlumno)
        assertTrue(regResult.isSuccess)

        // Inside library coordinates (~8m)
        val entranceResult = repository.recordEntrance("TESTUID123", 16.43256, -95.02253)
        assertTrue(entranceResult.isSuccess)

        val (alumno, registro) = entranceResult.getOrThrow()
        assertEquals("TESTUID123", alumno.uid)
        assertEquals("entrada", registro.tipo)

        // Second scan is also ENTRADA (solo entrada)
        val entrance2Result = repository.recordEntrance("TESTUID123", 16.43256, -95.02253)
        assertTrue(entrance2Result.isSuccess)
        val (_, registro2) = entrance2Result.getOrThrow()
        assertEquals("entrada", registro2.tipo)

        // Outside library coordinates (>50m) should fail
        val outsideResult = repository.recordEntrance("TESTUID123", 16.44000, -95.03000)
        assertTrue(outsideResult.isFailure)
    }
}
