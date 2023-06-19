package com.example.minierp

import android.R
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.minierp.databinding.ActivityCrearFacturacompraBinding
import com.example.minierp.model.Articulo
import com.example.minierp.model.Cliente
import com.example.minierp.model.Facturacompra
import com.example.minierp.model.Proveedor
import com.example.minierp.preferences.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class crearFacturacompra : AppCompatActivity(),AdapterView.OnItemSelectedListener {
    lateinit var binding: ActivityCrearFacturacompraBinding
    var nombreart = ""
    var fechafactura = ""
    var idfactura = ""
    var importe= 0.0f
    var cantidad= 0
    private var nombreprov = ""
    private var nombresArticulos = ArrayList<String>()
    private var nombresproovedores = ArrayList<String>()
    private var cantidadOriginal=  0 // Variable para almacenar la cantidad original
    private var editar = false
    private var db = Firebase.database
    private val auth = FirebaseAuth.getInstance()
    private lateinit var prefs: Prefs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearFacturacompraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)
        setListeners()
        cogerArticulo()
        fechaactual()

        // spinner
        val spinner = binding.etNombreartfacc
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, ArrayList<String>())
        spinner.adapter = adapter

        spinner.onItemSelectedListener = this
        binding.spinnerprov.onItemSelectedListener = this
        cargarNombresArticulos()
        cargarNombreCli()

    }
    private fun cogerArticulo() {
        val facturaCompra: Facturacompra = getSerializable(intent, "FACTURACOMPRA", Facturacompra::class.java)
        if (facturaCompra != null) {
            editar = true
            binding.idfactura.setText(facturaCompra.id.toString())
            //binding.etNombreartfacc.setText(facturaCompra.nombrearticulo.toString())
            binding.idfactura.isEnabled = false
            binding.fechafactura.isEnabled = false
            binding.etimporte.setText(facturaCompra.importe.toString())
            binding.etcantidad.setText(facturaCompra.cantidad.toString())
            binding.fechafactura.setText(facturaCompra.fecha.toString())
            binding.btnGuardar.text = "EDITAR"

            binding.etNombreartfacc.isEnabled = false
            binding.spinnerprov.isEnabled = false
            binding.etNombreartfacc.visibility = View.INVISIBLE
            binding.spinnerprov.visibility = View.INVISIBLE
            binding.tvediprov.setText("Proveedor "+facturaCompra.proveedor)
            binding.tveditartpro.setText("Artículo: "+facturaCompra.nombrearticulo)
            cantidadOriginal= facturaCompra.cantidad!!
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
            guardarFacturaCompra()
        }
    }

    private fun guardarFacturaCompra() {
        if (erroresDatos()) return

        val facturaCompra = Facturacompra(nombreart, idfactura, importe, cantidad, fechafactura, nombreprov)
        val emailUsuario = prefs.leerEmail()?.replace(".", "_")

        db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = db.getReference("usuario")

        if (emailUsuario != null) {
            val usuarioRef = ref.child(emailUsuario)
            val facturasCompraRef = usuarioRef.child("facturasCompra")

            if (!editar) {
                // Estamos creando una factura de venta, verificar si el ID ya existe
                val query = facturasCompraRef.orderByChild("id").equalTo(idfactura).limitToFirst(1)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // La factura de venta ya existe dentro de las facturas de venta del usuario

                            binding.idfactura.error = "¡La factura de venta ya existe!"
                            binding.idfactura.requestFocus()
                        } else {
                            // No existe la factura de venta, guardarla en Realtime Database
                            val facturaVentaRef = facturasCompraRef.child(idfactura)
                            facturaVentaRef.setValue(facturaCompra).addOnSuccessListener {
                                restarCantidadDeStock(nombreart,cantidad)
                                finish()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Error al obtener los datos: ${error.message}")
                    }
                })
            } else {
                // Estamos editando una factura de venta, actualizarla en Realtime Database
                val facturaCompraRef = facturasCompraRef.child(idfactura)
                facturaCompraRef.setValue(facturaCompra).addOnSuccessListener {
                    restarCantidadDeStock(nombreart,cantidad)
                    finish()
                }
            }
        }
    }



    private fun existeNombreFacturaCompra(facturaCompra: Facturacompra, ref: DatabaseReference, email: String) {
        val usuarioRef = ref.child("usuario").child(email)
        val facturasCompraRef = usuarioRef.child("facturasCompra")

        val query = facturasCompraRef.orderByChild("id").equalTo(facturaCompra.id).limitToFirst(1)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val facturaCompraRef = facturasCompraRef.push()
                    facturaCompraRef.setValue(facturaCompra)
                    finish()
                } else {
                    binding.idfactura.error = "¡La factura de compra ya existe!"
                    binding.idfactura.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener los datos: ${error.message}")
            }
        })
    }



    private fun erroresDatos(): Boolean {
        var error = false
        nombreart = binding.etNombreartfacc.selectedItem.toString().trim()
        nombreprov= binding.spinnerprov.selectedItem.toString().trim()
        if (editar==true){
            val facturacompra: Facturacompra? = getSerializable(intent, "FACTURACOMPRA", Facturacompra::class.java)
            if (facturacompra != null) {
                nombreart= facturacompra.nombrearticulo.toString()
            }
            if (facturacompra != null) {
                nombreprov= facturacompra.proveedor.toString()
            }

        }

        idfactura = binding.idfactura.text.toString().trim()
        importe = binding.etimporte.text.toString().trim().toFloatOrNull() ?: 0.0f
        cantidad = binding.etcantidad.text.toString().trim().toIntOrNull() ?: 0


        if (nombreart.isEmpty() || nombreart == "Seleciona el Artículo") {
            Toast.makeText(this, "¡Seleciona un Artículo!", Toast.LENGTH_SHORT).show()
            error = true
        }
        if (nombreprov.isEmpty() || nombreprov == "Seleciona el Proveedor") {
            Toast.makeText(this, "¡Seleciona un prooveedor!", Toast.LENGTH_SHORT).show()
            error = true
        }

        if (idfactura.isEmpty()) {
            binding.idfactura.error = "Introduce un ID de factura"
            error = true
        }

        if (importe <= 0) {
            binding.etimporte.error = "Introduce un importe válido"
            error = true
        }
        if (cantidad <= 0) {
            binding.etcantidad.error = "Introduce una cantidad válida"
            error = true
        }

        if (fechafactura.isEmpty()) {
            binding.fechafactura.error = "Introduce una fecha de factura válida"
            error = true
        }

        return error
    }

    private fun fechaactual(){
    binding.fechafactura.isEnabled = false

        // Obtener la fecha actual
        val currentDate = Calendar.getInstance().time

        // Formatear la fecha en el formato deseado
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        fechafactura = dateFormat.format(currentDate)
    binding.fechafactura.setText(fechafactura)
 }

    private fun cargarNombresArticulos() {
        // Obtener el email del usuario desde las preferencias
        val emailUsuario = prefs.leerEmail()?.replace(".", "_")

        // Enlace se encuentra en nuestro proyecto de Firebase en Realtime Database. Copiar y pegar
        db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = db.getReference("usuario") // Nodo principal de usuarios

        if (emailUsuario != null) {
            val usuarioRef = ref.child(emailUsuario) // Obtener la referencia del usuario existente
            val articulosRef = usuarioRef.child("articulos") // Obtener la referencia de los artículos

            articulosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    nombresArticulos.clear()
                    nombresArticulos.add("Seleciona el Artículo") // Valor por defecto

                    for (articuloSnapshot in snapshot.children) {
                        val articulo = articuloSnapshot.getValue(Articulo::class.java)
                        if (articulo != null) {
                            articulo.nombre?.let { nombresArticulos.add(it) }
                        }
                    }

                    val adapter = binding.etNombreartfacc.adapter as ArrayAdapter<String>
                    adapter.clear()
                    adapter.addAll(nombresArticulos)
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", "Error al cargar los nombres de los artículos")
                }
            })
        }
    }
    private fun cargarNombreCli() {
        // Obtener el email del usuario desde las preferencias
        val emailUsuario = prefs.leerEmail()?.replace(".", "_")

        // Enlace se encuentra en nuestro proyecto de Firebase en Realtime Database. Copiar y pegar
        db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = db.getReference("usuario") // Nodo principal de usuarios

        if (emailUsuario != null) {
            val usuarioRef = ref.child(emailUsuario) // Obtener la referencia del usuario existente
            val proveedoresRef = usuarioRef.child("proveedores") // Obtener la referencia de los clientes

            proveedoresRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val nombresProveedores = ArrayList<String>()

                    nombresProveedores.clear()
                    nombresProveedores.add("Seleciona el Proveedor")

                    for (proveedorSnapshot in snapshot.children) {

                        val proveedor = proveedorSnapshot.getValue(Proveedor::class.java)
                        if (proveedor != null) {
                            proveedor.nombrepro?.let { nombresProveedores.add(it) }
                        }
                    }

                    val adapter = ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_spinner_item,
                        nombresProveedores
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerprov.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", "Error al cargar los nombres de los proveedores")
                }
            })
        }
    }
    private fun restarCantidadDeStock(articulo: String, cantidad: Int) {
        val articuloRef = prefs.leerEmail()?.replace(".", "_")?.let {
            db.getReference("usuario").child(it)
                .child("articulos").child(articulo)
        }

        // Obtener el stock actual del artículo
        if (articuloRef != null) {
            articuloRef.child("stock").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val stockActual = snapshot.getValue(Int::class.java)

                    if (stockActual != null) {
                        // Sumar la cantidad original ingresada antes de restar al stock actual
                        val cantidadTotalARestar = stockActual - cantidadOriginal
                        println("----------------------------------------------------------------"+cantidadTotalARestar)
                        // Restar la cantidad total al stock actual
                        val stockActualizado = cantidadTotalARestar + cantidad

                        // Actualizar el stock en la base de datos
                        articuloRef.child("stock").setValue(stockActualizado)
                            .addOnSuccessListener {
                                // El stock se actualizó correctamente
                                Log.e("Firebase", "Stock Actualizado")
                                // Aquí puedes realizar cualquier otra acción necesaria
                            }
                            .addOnFailureListener { error ->
                                // Ocurrió un error al actualizar el stock
                                Log.e("Firebase", "Error al actualizar el stock: ${error.message}")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al obtener el stock: ${error.message}")
                }
            })
        }
    }
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (parent === binding.etNombreartfacc) {
            nombreart = parent.getItemAtPosition(pos).toString()
        }
    }


    override fun onNothingSelected(parent: AdapterView<*>) {
        // No se seleccionó nada
    }


}