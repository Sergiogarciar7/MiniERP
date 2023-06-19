package com.example.minierp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.minierp.databinding.ActivityCrearClientesBinding
import com.example.minierp.model.Articulo
import com.example.minierp.model.Cliente
import com.example.minierp.preferences.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable

class crearClientes : AppCompatActivity() {
    lateinit var binding: ActivityCrearClientesBinding
    var nombre = ""
    var direction = ""
    var IDcli = ""
    var NIF = ""
    private var editar = false
    private var db = Firebase.database
    private val auth = FirebaseAuth.getInstance()
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)
        setListeners()
        cogerCliente()
    }

    private fun cogerCliente() {
        val cliente: Cliente = getSerializable(intent, "CLIENTE", Cliente::class.java)
        if (cliente != null) {
            editar = true
            binding.etIDcli.setText(cliente.Idcli.toString())
            binding.etNombre2.setText(cliente.nombrecli.toString())
            binding.etIDcli.isEnabled = false
            binding.etDirecion.setText(cliente.direcion.toString())
            binding.etnif.setText(cliente.nif.toString())
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
            guardarCliente()
        }
    }

    private fun guardarCliente() {
        if (erroresDatos()) return

        // Los campos no están vacíos, vamos a guardar el artículo en Realtime Database
        val cliente= Cliente(nombre, direction, IDcli, NIF)

        // Obtener el email del usuario desde las preferencias
        val emailUsuario = prefs.leerEmail()?.replace(".", "_")

        // Enlace se encuentra en nuestro proyecto de Firebase en Realtime Database. Copiar y pegar
        db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = db.getReference("usuario") // Nodo principal de usuarios

        if (emailUsuario != null) {
            val usuarioRef = ref.child(emailUsuario) // Obtener la referencia del usuario existente
            val articulosRef = usuarioRef.child("clientes") // Obtener la referencia de los artículos del usuario

            if (!editar) {
                // Estamos editando un artículo, verificar si el nombre del artículo ya existe
                val query = articulosRef.orderByChild("idcli").equalTo(IDcli).limitToFirst(1)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // El artículo ya existe dentro de los artículos del usuario
                            binding.etIDcli.error = "¡El artículo ya existe!"
                            binding.etIDcli.requestFocus()
                        } else {
                            // No existe el cliente, actualizarlo en Realtime Database
                            val articuloRef = articulosRef.child(IDcli)
                            articuloRef.setValue(cliente).addOnSuccessListener {
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
                val nuevoarticuloRef = articulosRef.child(IDcli)
                nuevoarticuloRef.setValue(cliente).addOnSuccessListener {
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
                    binding.etNombre2.error = "¡El artículo ya existe!"
                    binding.etNombre2.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener los datos: ${error.message}")
            }
        })
    }



    private fun erroresDatos(): Boolean {
        nombre = binding.etNombre2.text.toString().trim()
        IDcli= binding.etIDcli.text.toString().trim()
        direction = binding.etDirecion.text.toString().trim()
        NIF= binding.etnif.text.toString().trim()

        if (nombre.length < 3) {
            binding.etNombre2.setError("La longitud debe ser de al menos 3 caracteres")
            return true
        }

        if (IDcli.length <= 0) {
            binding.etDirecion.setError("Rellena el campo ID")
            return true
        }

        if (NIF.length<= 8) {
            binding.etnif.setError("Rellena el campo NIF correctamente 8 números y una letra")
            return true
        }


        return false
    }

}