package com.example.minierp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.minierp.databinding.ActivityCrearArticuloBinding
import com.example.minierp.model.Articulo
import com.example.minierp.preferences.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable

class crearArticulo : AppCompatActivity() {
    lateinit var binding: ActivityCrearArticuloBinding
    var nombre = ""
    var precio = 0.0f
    var stock = 0
    var descripcion = "hola"
    private var editar = false
    private var db = Firebase.database
    private val auth = FirebaseAuth.getInstance()
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearArticuloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)
        setListeners()
        cogerArticulo()
    }

    private fun cogerArticulo() {
        val articulo: Articulo = getSerializable(intent, "ARTICULO", Articulo::class.java)
        if (articulo != null) {
            editar = true
            binding.etDescripcion.setText(articulo.descripcion.toString())
            binding.etNombre.setText(articulo.nombre.toString())
            binding.etNombre.isEnabled = false
            binding.etprecio.setText(articulo.precio.toString())
            binding.etstock.setText(articulo.stock.toString())
            binding.btnGuardar.text = "EDITAR"
        }
    }

    private fun <T : Serializable?> getSerializable(intent: Intent, key: String, clase: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(key, clase)!!
        } else {
            intent.getSerializableExtra(key) as T
        }
    }

    private fun setListeners() {
        binding.btnCancelar.setOnClickListener {
            finish()
        }
        binding.btnGuardar.setOnClickListener {
            guardarArticulo()
        }
    }

    private fun guardarArticulo() {
        if (erroresDatos()) return

        // Los campos no están vacíos, vamos a guardar el artículo en Realtime Database
        val articulo = Articulo(nombre, descripcion, precio, stock)

        // Obtener el email del usuario desde las preferencias
        val emailUsuario = prefs.leerEmail()?.replace(".", "_")

        // Enlace se encuentra en nuestro proyecto de Firebase en Realtime Database. Copiar y pegar
        db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = db.getReference("usuario") // Nodo principal de usuarios

        if (emailUsuario != null) {
            val usuarioRef = ref.child(emailUsuario) // Obtener la referencia del usuario existente
            val articulosRef = usuarioRef.child("articulos") // Obtener la referencia de los artículos del usuario

            if (!editar) {
                // Estamos editando un artículo, verificar si el nombre del artículo ya existe
                val query = articulosRef.orderByChild("nombre").equalTo(nombre).limitToFirst(1)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // El artículo ya existe dentro de los artículos del usuario
                            binding.etNombre.error = "¡El artículo ya existe!"
                            binding.etNombre.requestFocus()
                        } else {
                            // No existe el artículo, actualizarlo en Realtime Database
                            val articuloRef = articulosRef.child(nombre)
                            articuloRef.setValue(articulo).addOnSuccessListener {
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error al obtener los datos: ${error.message}")
                    }
                })
            } else {
                // Estamos creando un nuevo artículo, agregarlo en Realtime Database
                val nuevoarticuloRef = articulosRef.child(nombre)
                nuevoarticuloRef.setValue(articulo).addOnSuccessListener {
                    finish()
                }
            }
        }
    }



    private fun existeNombreArticulo(articulo: Articulo, ref: DatabaseReference, email: String) {
        val usuarioRef = ref.child("usuario").child(email)
        val articulosRef = usuarioRef.child("articulos")

        val query = articulosRef.orderByChild("nombre").equalTo(articulo.nombre).limitToFirst(1)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val articuloRef = articulosRef.push()
                    articuloRef.setValue(articulo)
                    finish()
                } else {
                    binding.etNombre.error = "¡El artículo ya existe!"
                    binding.etNombre.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener los datos: ${error.message}")
            }
        })
    }



    private fun erroresDatos(): Boolean {
        nombre = binding.etNombre.text.toString().trim()
        descripcion = binding.etDescripcion.text.toString().trim()
        precio = binding.etprecio.text.toString().trim().toFloatOrNull() ?: 0.0f
        stock = binding.etstock.text.toString().trim().toIntOrNull() ?: 0

        if (nombre.length < 3) {
            binding.etNombre.setError("La longitud debe ser de al menos 3 caracteres")
            return true
        }

        if (precio <= 0) {
            binding.etprecio.setError("Rellena el campo precio")
            return true
        }

        if (stock <= 0) {
            binding.etstock.setError("Rellena el campo stock")
            return true
        }

        return false
    }

}