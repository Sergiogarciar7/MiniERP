package com.example.minierp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.minierp.databinding.ActivityMainBinding
import com.example.minierp.databinding.ActivityMainBinding.*
import com.example.minierp.preferences.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {



    lateinit var binding: ActivityMainBinding
    lateinit var prefs: Prefs
    private var email=""
    private var pass=""
    lateinit var  db: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
        prefs= Prefs(this)
        comprobarSesion()
        setListeners()
    }
    private fun setListeners() {
        binding.btnRegister.setOnClickListener {
            registrar()
        }
        binding.btnLogin.setOnClickListener{
            logear()
        }

    }
    override fun onBackPressed() {

        finish() // Close the app instead of going back to the SplashActivity
    }
    private fun logear() {

        if (!recogerDatos()) return
        //supuestamente el email y pasword van bien
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,pass).addOnCompleteListener{
            //si va bien
            if (it.isSuccessful){
                prefs.guardarEmail(email)

                irHome()
            }else{
                mostrarError(it.exception.toString())
            }
        }


    }




    private fun registrar() {
        if (!recogerDatos()) return
        //supuestamente el email y pasword van bien

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
            //si va bien
            if (it.isSuccessful){
                prefs.guardarEmail(email)
                // Reemplazar los caracteres no permitidos en la ruta del correo electrónico
                val emailFormatted = email.replace(".", "_")
                db.getReference("usuario").child(emailFormatted).setValue(email).addOnSuccessListener {
                irHome()
                }
            }else{
                mostrarError(it.exception.toString())
            }
        }


    }

    private fun mostrarError(txt: String) {
        val builder= AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("Error en el proceso $txt")
            .setPositiveButton("Aceptar",null)
            .create()
            .show()
    }


    private fun recogerDatos(): Boolean {
        email=binding.etEmail.text.toString().trim()
        //validar un email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("introduce un Email valido")
            return false
        }
        //No es un email valido
        pass=binding.etPassword.text.toString().trim()
        if (pass.length<3) {
            binding.etPassword.setError("La contraseña tienen que tener más de 3 caracteres")
            return false
        }
        return true
    }

    private fun comprobarSesion(){
        val e= prefs.leerEmail()
        //Si la sesion ya esta iniciada va a home directamente
        if (!e.isNullOrEmpty()){
            irHome()
        }
    }

    private fun irHome() {
       startActivity(Intent(this,HomeActivity::class.java))
    }
}