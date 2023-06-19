package com.example.minierp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minierp.R
import com.example.minierp.adapter.ArticuloAdapter
import com.example.minierp.crearArticulo
import com.example.minierp.databinding.ActivityArticulosmFragmentBinding
import com.example.minierp.model.Articulo
import com.example.minierp.preferences.Prefs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArticulosmFragment : Fragment() {

    lateinit var prefs: Prefs

    private var listaArticulos = arrayListOf<Articulo>()
    private var _binding: ActivityArticulosmFragmentBinding? = null

    private var db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
    lateinit var adapter: ArticuloAdapter
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

       // return inflater.inflate(R.layout.activity_articulosm_fragment, container, false)
       _binding = ActivityArticulosmFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireActivity() as Context)

        // Obtén la referencia del RecyclerView desde el binding
        val recyclerView = binding.recArticulos

        // Configura el LinearLayoutManager para el RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        // Inicializa el adaptador del RecyclerView
        adapter = ArticuloAdapter(listaArticulos, { onItemBorrar(it) }, { onItemEdit(it) })
        recyclerView.adapter = adapter

        // Traer los artículos y configurar el RecyclerView
        traerArticulos()
        setListeners()
        setRecycler()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun traerArticulos() {
        val email = prefs.leerEmail()
        val emailFormatted = email?.replace(".", "_")
        if (email != null) {
            val referencia = db.getReference("usuario").child(emailFormatted.toString()).child("articulos")
            referencia.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaArticulos.clear()
                    if (snapshot.exists()) {
                        for (item in snapshot.children) {
                            val articulo = item.getValue(Articulo::class.java)
                            if (articulo != null) {
                                listaArticulos.add(articulo)
                            }
                        }
                        adapter.lista = listaArticulos
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
        binding.recArticulos.layoutManager = layoutManager
        adapter = ArticuloAdapter(listaArticulos, { onItemBorrar(it) }, { onItemEdit(it) })
        binding.recArticulos.adapter = adapter
    }

    private fun onItemBorrar(position: Int) {
        val articulo = listaArticulos[position]
        val email = prefs.leerEmail()
        val emailFormatted = email?.replace(".", "_")
        val referencia = emailFormatted?.let { db.getReference("usuario").child(it).child("articulos") }

        // Eliminar el artículo del ArrayList
        listaArticulos.removeAt(position)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemRangeChanged(position, listaArticulos.size)

        // Eliminar el artículo de la base de datos
        articulo.nombre?.let { referencia?.child(it)?.removeValue() }
    }

    private fun onItemEdit(articulo: Articulo) {
        val i = Intent(requireContext(), crearArticulo::class.java).apply {
            putExtra("ARTICULO", articulo)
        }
        startActivity(i)
    }

    private fun setListeners() {
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(requireContext(), crearArticulo::class.java))
        }
    }
}