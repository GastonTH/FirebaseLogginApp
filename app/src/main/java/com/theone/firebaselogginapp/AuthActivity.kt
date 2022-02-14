package com.theone.firebaselogginapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.theone.firebaselogginapp.databinding.ActivityAuthBinding
import java.security.Provider

const val EMAIL = "com.theone.firebaseloginapp{EMAIL}"
const val PROVIDER = "com.theone.firebaseloginapp{PROVIDER}"
const val GOOGLE_ID_SIGNIN = 100


class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var btn_signIn: Button
    private lateinit var btn_login: Button
    private lateinit var edit_email: EditText
    private lateinit var edit_password: EditText
    private lateinit var btnGoogleLog: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Thread.sleep(500)
        setTheme(R.style.Theme_FirebaseLogginApp)

        btn_signIn = binding.btnSignin
        btn_login = binding.btnGo
        edit_email = binding.editEmail
        edit_password = binding.editPassword
        btnGoogleLog = binding.btnGoogleLog

        // Setup
        notification()
        setup()
        session()

    }



    private fun notification() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { result ->

            System.out.println("RESULT --> " + result)

        }
    }

    override fun onStart() {
        super.onStart()

        binding.authLayout.visibility = View.VISIBLE
    }

    private fun session() {

        val prefs = getSharedPreferences(getString(R.string.pref_file_name), Context.MODE_PRIVATE)

        val email: String ?= prefs.getString("email", null)
        val provider: String ?= prefs.getString("provider", null)

        if (email !=null && provider != null){

            binding.authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))

        }

    }

    private fun setup() {

        title = "Autenticacion"

        //Logica del boton de registrar mediante correo y contraseña
        btn_signIn.setOnClickListener{

            if (edit_email.text.isNotEmpty() && edit_password.text.isNotEmpty()){

                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(edit_email.text.toString(),
                        edit_password.text.toString()).addOnCompleteListener{

                    if (it.isSuccessful){
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    }else{
                        authAlert()
                    }

                }
            }
        }

        //Logica del boton de acceder mediante correo y contraseña
        btn_login.setOnClickListener{

            if (edit_email.text.isNotEmpty() && edit_password.text.isNotEmpty()){

                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(edit_email.text.toString(),
                        edit_password.text.toString()).addOnCompleteListener{

                    if (it.isSuccessful){
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    }else{
                        authAlert()
                    }
                }
            }else{

            }
        }

        btnGoogleLog.setOnClickListener{

            // Configuracion

            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).
                    requestIdToken(getString(R.string.default_web_client_id)).
                    requestEmail().
                    build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_ID_SIGNIN)
        }
    }


    // Metodo sobreescrito del acceso al cliente de google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == GOOGLE_ID_SIGNIN){

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                // Registrando al usuario con la cuenta de google

                if (account != null){ // Si la cuenta no es nula, procederemos a la autenticacion

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{

                        if (it.isSuccessful){
                            showHome(account.email?: "", ProviderType.GOOGLE)
                        }else{
                            authAlert()
                        }

                    }
                }
            }catch (e: ApiException){
                googleAlertAuth()
            }
        }
    }

    private fun googleAlertAuth() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error intentando autenticar con google")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email:String, proveedor: ProviderType) {

        val intent: Intent = Intent(this, HomeActivity::class.java).apply {
            putExtra(EMAIL, email)
            putExtra(PROVIDER, proveedor.name)
        }

        startActivity(intent)

    }

    private fun authAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}