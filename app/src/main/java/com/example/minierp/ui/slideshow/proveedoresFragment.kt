package com.example.minierp.ui.slideshow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minierp.adapter.ClienteAdapter
import com.example.minierp.adapter.ProveedorAdapter
import com.example.minierp.crearClientes
import com.example.minierp.crearProveedor
import com.example.minierp.databinding.FragmentGalleryBinding
import com.example.minierp.databinding.FragmentSlideshowBinding
import com.example.minierp.model.Cliente
import com.example.minierp.model.Proveedor
import com.example.minierp.preferences.Prefs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class proveedoresFragment : Fragment() {

    lateinit var prefs: Prefs

    private var listaProveedor = arrayListOf<Proveedor>()
    private var _binding:FragmentSlideshowBinding? = null

    private var db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
    lateinit var adapter: ProveedorAdapter
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        return binding.root

        //val textView: TextView = binding.textGallery

        // textView.text = it
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireActivity() as Context)

        // Obtén la referencia del RecyclerView desde el binding
        val recyclerView = binding.recProveedor

        // Configura el LinearLayoutManager para el RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        // Inicializa el adaptador del RecyclerView
        adapter = ProveedorAdapter(listaProveedor, { onItemBorrar(it) }, { onItemEdit(it) })
        recyclerView.adapter = adapter

        // Traer los artículos y configurar el RecyclerView
        traerProveedor()
        setListeners()
        setRecycler()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun traerProveedor() {
        val email = prefs.leerEmail()

        val emailFormatted = email?.replace(".", "_")
        if (email != null) {
            val referencia = db.getReference("usuario").child(emailFormatted.toString()).child("proveedores")
            referencia.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaProveedor.clear()
                    if (snapshot.exists()) {
                        for (item in snapshot.children) {
                            val proveedor = item.getValue(Proveedor::class.java)
                            if (proveedor != null) {
                                listaProveedor.add(proveedor)
                            }
                        }
                        adapter.lista = listaProveedor
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar error de cancelación
                    Log.e("Firebase", "Error al obtener los datos: ${error.message}")
                }
            })
        }
    }

    private fun setRecycler() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recProveedor.layoutManager = layoutManager
        adapter = ProveedorAdapter(listaProveedor, { onItemBorrar(it) }, { onItemEdit(it) })
        binding.recProveedor.adapter = adapter
    }

    private fun onItemBorrar(position: Int) {
        val Proveedor = listaProveedor[position]
        val email = prefs.leerEmail()
        val emailFormatted = email?.replace(".", "_")
        val referencia = emailFormatted?.let { db.getReference("usuario").child(it).child("proveedores") }

        // Eliminar el artículo del ArrayList
        listaProveedor.removeAt(position)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemRangeChanged(position, listaProveedor.size)

        // Eliminar el artículo de la base de datos
        Proveedor.Idpro?.let { referencia?.child(it.toString())?.removeValue() }
    }
    //falta por cambiar
    private fun onItemEdit(Proveedor: Proveedor) {
        val i = Intent(requireContext(), crearProveedor::class.java).apply {
            putExtra("PROVEEDOR", Proveedor)
        }
        startActivity(i)
    }

    private fun setListeners() {
        binding.btnAddpro.setOnClickListener {
            startActivity(Intent(requireContext(), crearProveedor::class.java))
        }
    }
}