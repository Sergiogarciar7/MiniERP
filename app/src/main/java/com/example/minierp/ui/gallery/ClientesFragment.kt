package com.example.minierp.ui.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minierp.adapter.ClienteAdapter
import com.example.minierp.crearClientes
import com.example.minierp.databinding.FragmentGalleryBinding
import com.example.minierp.model.Cliente
import com.example.minierp.preferences.Prefs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClientesFragment : Fragment() {
    lateinit var prefs: Prefs

    private var listaClientes = arrayListOf<Cliente>()
    private var _binding: FragmentGalleryBinding? = null

    private var db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
    lateinit var adapter: ClienteAdapter
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root

        //val textView: TextView = binding.textGallery

           // textView.text = it
        }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireActivity() as Context)

        // Obtén la referencia del RecyclerView desde el binding
        val recyclerView = binding.recClientes

        // Configura el LinearLayoutManager para el RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        // Inicializa el adaptador del RecyclerView
        adapter = ClienteAdapter(listaClientes, { onItemBorrar(it) }, { onItemEdit(it) })
        recyclerView.adapter = adapter

        // Traer los artículos y configurar el RecyclerView
        traerCliente()
        setListeners()
        setRecycler()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun traerCliente() {
        val email = prefs.leerEmail()

        val emailFormatted = email?.replace(".", "_")
        if (email != null) {
            val referencia = db.getReference("usuario").child(emailFormatted.toString()).child("clientes")
            referencia.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaClientes.clear()
                    if (snapshot.exists()) {
                        for (item in snapshot.children) {
                            val cliente = item.getValue(Cliente::class.java)
                            if (cliente != null) {
                                listaClientes.add(cliente)
                            }
                        }
                        adapter.lista = listaClientes
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
        binding.recClientes.layoutManager = layoutManager
        adapter = ClienteAdapter(listaClientes, { onItemBorrar(it) }, { onItemEdit(it) })
        binding.recClientes.adapter = adapter
    }

    private fun onItemBorrar(position: Int) {
        val cliente = listaClientes[position]
        val email = prefs.leerEmail()
        val emailFormatted = email?.replace(".", "_")
        val referencia = emailFormatted?.let { db.getReference("usuario").child(it).child("clientes") }

        // Eliminar el artículo del ArrayList
        listaClientes.removeAt(position)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemRangeChanged(position, listaClientes.size)

        // Eliminar el artículo de la base de datos
        cliente.Idcli?.let { referencia?.child(it.toString())?.removeValue() }
    }
//falta por cambiar
    private fun onItemEdit(cliente: Cliente) {
        val i = Intent(requireContext(), crearClientes::class.java).apply {
            putExtra("CLIENTE", cliente)
        }
        startActivity(i)
    }

    private fun setListeners() {
        binding.btnAddcli.setOnClickListener {
            startActivity(Intent(requireContext(), crearClientes::class.java))
        }
    }
}