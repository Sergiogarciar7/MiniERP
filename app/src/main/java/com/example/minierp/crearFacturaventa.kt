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
import android.widget.Spinner
import android.widget.Toast
import com.example.minierp.databinding.ActivityCrearFacturaventaBinding
import com.example.minierp.model.Articulo
import com.example.minierp.model.Cliente
import com.example.minierp.model.Facturaventa
import com.example.minierp.preferences.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import java.util.Arrays.asList
import kotlin.collections.ArrayList

class crearFacturaventa : AppCompatActivity(),AdapterView.OnItemSelectedListener {
    lateinit var binding: ActivityCrearFacturaventaBinding
    private var nombreart = ""
    private var fechafactura = ""
    private var idfactura = ""
    private var importe = 0.0f
    private var cantidad = 0
    private var nombreclientes = ""
    private var editar = false
    private var db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
    private val auth = FirebaseAuth.getInstance()
    private lateinit var prefs: Prefs
    private var nombresArticulos = ArrayList<String>()
    private var nombresClientes = ArrayList<String>()
    private var cantidadOriginal=  0 // Variable para almacenar la cantidad original
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearFacturaventaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)
        setListeners()
        cogerArticulo()
        fechaActual()

        // spinner
        val spinner = binding.etNombreartfacc
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ArrayList<String>())
        spinner.adapter = adapter

        spinner.onItemSelectedListener = this
        binding.spinnerclientes.onItemSelectedListener = this
        cargarNombresArticulos()
        cargarNombreCli()
    }

    private fun cogerArticulo() {
        val facturaventa: Facturaventa? = getSerializable(intent, "FACTURAVENTA", Facturaventa::class.java)
        if (facturaventa != null) {
            editar = true
            binding.idfactura.setText(facturaventa.id.toString())
            binding.idfactura.isEnabled = false
            binding.fechafactura.isEnabled = false
            binding.etimporte.setText(facturaventa.importe.toString())
            binding.etcantidad.setText(facturaventa.cantidad.toString())
            binding.fechafactura.setText(facturaventa.fecha.toString())
            binding.etNombreartfacc.isEnabled = false
            binding.spinnerclientes.isEnabled = false
            binding.etNombreartfacc.visibility = View.INVISIBLE
            binding.spinnerclientes.visibility = View.INVISIBLE
            binding.tvedicli.setText("Cliente: "+facturaventa.cliente)
            binding.tveditart.setText("Artículo: "+facturaventa.nombrearticulo)
           cantidadOriginal= facturaventa.cantidad!!



            binding.btnGuardar.text = "EDITAR"
        }
    }

    private fun <T : Serializable?> getSerializable(intent: Intent, key: String, clase: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(key, clase) as T
        } else {
            intent.getSerializableExtra(key) as T
        }
    }

    private fun setListeners() {
        binding.btnCancelar.setOnClickListener {
            finish()
        }
        binding.btnGuardar.setOnClickListener {

            guardarFacturaVenta()
        }
    }

    private fun guardarFacturaVenta() {
        if (erroresDatos()) return

        val facturaVenta = Facturaventa(nombreart, idfactura, importe, cantidad, fechafactura, nombreclientes)
        val emailUsuario = prefs.leerEmail()?.replace(".", "_")

        db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = db.getReference("usuario")

        if (emailUsuario != null) {
            val usuarioRef = ref.child(emailUsuario)
            val facturasVentaRef = usuarioRef.child("facturasVenta")

            if (!editar) {
                // Estamos creando una factura de venta, verificar si el ID ya existe
                val query = facturasVentaRef.orderByChild("id").equalTo(idfactura).limitToFirst(1)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // La factura de venta ya existe dentro de las facturas de venta del usuario

                            binding.idfactura.error = "¡La factura de venta ya existe!"
                            binding.idfactura.requestFocus()
                        } else {
                            // No existe la factura de venta, guardarla en Realtime Database
                            val facturaVentaRef = facturasVentaRef.child(idfactura)
                            facturaVentaRef.setValue(facturaVenta).addOnSuccessListener {
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
                val facturaVentaRef = facturasVentaRef.child(idfactura)
                facturaVentaRef.setValue(facturaVenta).addOnSuccessListener {
                    restarCantidadDeStock(nombreart,cantidad)
                    finish()
                }
            }
        }
    }
    private fun erroresDatos(): Boolean {
        var error = false
        nombreart = binding.etNombreartfacc.selectedItem.toString().trim()
        nombreclientes = binding.spinnerclientes.selectedItem.toString().trim()
     if (editar==true){
         val facturaventa: Facturaventa? = getSerializable(intent, "FACTURAVENTA", Facturaventa::class.java)
         if (facturaventa != null) {
             nombreart= facturaventa.nombrearticulo.toString()
         }
         if (facturaventa != null) {
             nombreclientes= facturaventa.cliente.toString()
         }

     }
        idfactura = binding.idfactura.text.toString().trim()
        importe = binding.etimporte.text.toString().trim().toFloatOrNull() ?: 0.0f
        cantidad = binding.etcantidad.text.toString().trim().toIntOrNull() ?: 0
        fechafactura = binding.fechafactura.text.toString().trim()

        if (nombreart.isEmpty() || nombreart == "Seleciona el Artículo") {
            Toast.makeText(this, "¡Seleciona un Artículo!", Toast.LENGTH_SHORT).show()
            error = true
        }
        if (nombreclientes.isEmpty() || nombreclientes == "Seleciona el Cliente") {
            Toast.makeText(this, "¡Seleciona un cliente!", Toast.LENGTH_SHORT).show()
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

    private fun fechaActual() {
        binding.fechafactura.isEnabled = false
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        binding.fechafactura.setText(currentDate)
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
            val clientesRef = usuarioRef.child("clientes") // Obtener la referencia de los clientes

            clientesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val nombresClientes = ArrayList<String>()
                    nombresClientes.clear()
                    nombresClientes.add("Seleciona el Cliente")

                    for (clienteSnapshot in snapshot.children) {
                        val cliente = clienteSnapshot.getValue(Cliente::class.java)
                        if (cliente != null) {
                            cliente.nombrecli?.let { nombresClientes.add(it) }
                        }
                    }

                    val adapter = ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_spinner_item,
                        nombresClientes
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerclientes.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", "Error al cargar los nombres de los clientes")
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
                        val cantidadTotalARestar = cantidadOriginal + stockActual
                         println("----------------------------------------------------------------"+cantidadTotalARestar)
                        // Restar la cantidad total al stock actual
                        val stockActualizado = cantidadTotalARestar - cantidad

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