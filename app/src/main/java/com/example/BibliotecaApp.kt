package com.example

import android.app.Application
import android.util.Log
import com.example.data.repository.BibliotecaRepository
import com.google.firebase.FirebaseApp

class BibliotecaApp : Application() {

    lateinit var repository: BibliotecaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d("BibliotecaApp", "FirebaseApp initialized")
        } catch (e: Exception) {
            Log.w("BibliotecaApp", "FirebaseApp init handled: ${e.message}")
        }
        repository = BibliotecaRepository(this)
    }
}
