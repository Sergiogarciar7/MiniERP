package com.example.minierp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.minierp.databinding.ActivityCrearProveedorBinding
import com.example.minierp.model.Proveedor
import com.example.minierp.preferences.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable

class crearProveedor : AppCompatActivity() {

    lateinit var binding: ActivityCrearProveedorBinding
    var nombre = ""
    var direccion = ""
    var idProveedor = ""
    var nifProveedor = ""
    private var editar = false
    private var db = Firebase.database
    private val auth = FirebaseAuth.getInstance()
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearProveedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)
        setListeners()
        cogerProveedor()
    }

    private fun cogerProveedor() {
        val proveedor: Proveedor = getSerializable(intent, "PROVEEDOR", Proveedor::class.java)
        if (proveedor != null) {
            editar = true
            binding.etIdPro.setText(proveedor.Idpro.toString())
            binding.etNombrePro.setText(proveedor.nombrepro.toString())
            binding.etIdPro.isEnabled = false
            binding.etDireccionPro.setText(proveedor.direcionpro.toString())
            binding.etNifPro.setText(proveedor.nifpro.toString())
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
            guardarProveedor()
        }
    }

    private fun guardarProveedor() {
        if (erroresDatos()) return

        // Los campos no están vacíos, vamos a guardar el proveedor en Realtime Database
        val proveedor = Proveedor(nombre, direccion, idProveedor, nifProveedor)

        // Obtener el email del usuario desde las preferencias
        val emailUsuario = prefs.leerEmail()?.replace(".", "_")

        // Enlace se encuentra en nuestro proyecto de Firebase en Realtime Database. Copiar y pegar
        db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = db.getReference("usuario") // Nodo principal de usuarios

        if (emailUsuario != null) {
            val usuarioRef = ref.child(emailUsuario) // Obtener la referencia del usuario existente
            val proveedoresRef = usuarioRef.child("proveedores") // Obtener la referencia de los proveedores del usuario
            println("Id.pro: $idProveedor")
            println("idProveedor: ${proveedor.Idpro}")
            if (!editar) {
                // Estamos creando un nuevo proveedor, verificar si el ID del proveedor ya existe
                val query = proveedoresRef.orderByChild("idpro").equalTo(idProveedor).limitToFirst(1)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // El proveedor ya existe dentro de los proveedores del usuario
                            binding.etIdPro.error = "¡El proveedor ya existe!"
                            binding.etIdPro.requestFocus()
                        } else {
                            // No existe el proveedor, actualizarlo en Realtime Database
                            val proveedorRef = proveedor.Idpro?.let { proveedoresRef.child(it) }
                            if (proveedorRef != null) {
                                proveedorRef.setValue(proveedor).addOnSuccessListener {
                                    println("Id.pro else 1: $idProveedor")
                                    println("idProveedor else 1: ${proveedor.Idpro}")
                                    finish()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error al obtener los datos: ${error.message}")
                    }
                })
            } else {
                // Estamos editando un proveedor, actualizarlo en Realtime Database
                println("Id.pro else final: $idProveedor")
                println("idProveedor else final: ${proveedor.Idpro}")
                val proveedorRef = proveedoresRef.child(idProveedor)
                proveedorRef.setValue(proveedor).addOnSuccessListener {
                    finish()
                }
            }
        }
    }




    private fun existeNombreProveedor(proveedor: Proveedor, ref: DatabaseReference, email: String) {
        val usuarioRef = ref.child("usuario").child(email)
        val proveedoresRef = usuarioRef.child("proveedores")

        val query = proveedoresRef.orderByChild("idProveedor").equalTo(proveedor.Idpro).limitToFirst(1)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // El proveedor no existe, actualizarlo en Realtime Database
                    val proveedorRef = proveedor.Idpro?.let { proveedoresRef.child(it) }
                    if (proveedorRef != null) {
                        proveedorRef.setValue(proveedor).addOnSuccessListener {
                            finish()
                        }
                    }
                } else {
                    // El proveedor ya existe
                    binding.etIdPro.error = "¡El proveedor ya existe!"
                    binding.etIdPro.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener los datos: ${error.message}")
            }
        })
    }




    private fun erroresDatos(): Boolean {
        nombre = binding.etNombrePro.text.toString().trim()
        idProveedor = binding.etIdPro.text.toString().trim()
        direccion = binding.etDireccionPro.text.toString().trim()
        nifProveedor = binding.etNifPro.text.toString().trim()

        if (nombre.length < 3) {
            binding.etNombrePro.setError("La longitud debe ser de al menos 3 caracteres")
            return true
        }

        if (idProveedor.length <= 0) {
            binding.etDireccionPro.setError("Rellena el campo ID")
            return true
        }

        if (nifProveedor.length <= 8) {
            binding.etNifPro.setError("Rellena el campo NIF correctamente: 8 números y una letra")
            return true
        }

        return false
    }


}