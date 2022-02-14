package com.theone.firebaselogginapp

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.theone.firebaselogginapp.databinding.ActivityAuthBinding
import com.theone.firebaselogginapp.databinding.ActivityHomeBinding

enum class ProviderType {
    BASIC,
    GOOGLE
}

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var txtEmail: TextView
    private lateinit var txtProvider: TextView
    private lateinit var logOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Setup

        txtEmail = binding.txtEmail
        txtProvider = binding.txtProveedor
        logOut = binding.btnClose

        var bundle: Bundle? = intent.extras

        var emailIntent = bundle?.getString(EMAIL)
        var providerIntent = bundle?.getString(PROVIDER)

        setup(emailIntent ?:"", providerIntent ?:"")

        // Guardado de datos mediante sharepreferences

        val prefs:SharedPreferences.Editor = getSharedPreferences(getString(R.string.pref_file_name), Context.MODE_PRIVATE).edit()
        prefs.putString("email", emailIntent)
        prefs.putString("provider", providerIntent)
        prefs.apply()
    }

    private fun setup(email: String, provider: String) {

        title = "Inicio"

        txtEmail.text = email
        txtProvider.text = provider

        logOut.setOnClickListener{

            val prefs:SharedPreferences.Editor = getSharedPreferences(getString(R.string.pref_file_name), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

    }
}