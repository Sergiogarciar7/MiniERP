package com.example.minierp.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minierp.adapter.FacturacompraAdapter
import com.example.minierp.crearFacturacompra
import com.example.minierp.databinding.ActivityFacturascomprasFragmentBinding
import com.example.minierp.model.Facturacompra
import com.example.minierp.preferences.Prefs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itextpdf.text.Document
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

class FacturascomprasFragment :  Fragment() {

    lateinit var prefs: Prefs

    private var listafacturacompra= arrayListOf<Facturacompra>()
    private var _binding: ActivityFacturascomprasFragmentBinding? = null

    private var db = FirebaseDatabase.getInstance("https://minierp-fea28-default-rtdb.europe-west1.firebasedatabase.app/")
    lateinit var adapter: FacturacompraAdapter
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // return inflater.inflate(R.layout.activity_articulosm_fragment, container, false)
        _binding = ActivityFacturascomprasFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireActivity() as Context)

        // Obtén la referencia del RecyclerView desde el binding
        val recyclerView = binding.recFactcompra

        // Configura el LinearLayoutManager para el RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        // Inicializa el adaptador del RecyclerView
        adapter = FacturacompraAdapter(listafacturacompra, { onItemBorrar(it) }, { onItemEdit(it)},{onItemImprimir(it)})
        recyclerView.adapter = adapter

        // Traer los artículos y configurar el RecyclerView
        traerFacturacompra()
        setListeners()
        setRecycler()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun traerFacturacompra() {
        val email = prefs.leerEmail()
        val emailFormatted = email?.replace(".", "_")
        if (email != null) {
            val referencia = db.getReference("usuario").child(emailFormatted.toString()).child("facturasCompra")
            referencia.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listafacturacompra.clear()
                    if (snapshot.exists()) {
                        for (item in snapshot.children) {
                            val facturacompra = item.getValue(Facturacompra::class.java)
                            if (facturacompra != null) {
                                listafacturacompra.add(facturacompra)
                            }
                        }
                        adapter.lista =  listafacturacompra
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
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.recFactcompra.layoutManager = layoutManager
        adapter = FacturacompraAdapter(listafacturacompra, { onItemBorrar(it) }, { onItemEdit(it) },{onItemImprimir(it)})
        binding.recFactcompra.adapter = adapter
    }

    private fun onItemBorrar(position: Int) {
        val facturacompra =  listafacturacompra[position]
        val email = prefs.leerEmail()
        val emailFormatted = email?.replace(".", "_")
        val referencia = emailFormatted?.let { db.getReference("usuario").child(it).child("facturasCompra") }

        // Eliminar el artículo del ArrayList
        listafacturacompra.removeAt(position)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemRangeChanged(position,  listafacturacompra.size)

        // Eliminar el artículo de la base de datos
       facturacompra.id?.let { referencia?.child(it)?.removeValue() }
    }

    private fun onItemEdit(facturacompra: Facturacompra) {
        val i = Intent(requireContext(), crearFacturacompra::class.java).apply {
            putExtra("FACTURACOMPRA", facturacompra)
        }
        startActivity(i)
    }

    private fun setListeners() {
        binding.btnAddfactcompra.setOnClickListener {
            startActivity(Intent(requireContext(), crearFacturacompra::class.java))
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun onItemImprimir(facturacompra: Facturacompra) {

        val nombreart = facturacompra.nombrearticulo
        val importe = facturacompra.importe
        val cantidad = facturacompra.cantidad
        val fechafactura = facturacompra.fecha
        val nombreproveedor = facturacompra.proveedor

        if (nombreart != null) {
            createPDF(nombreart, nombreproveedor, importe, cantidad, fechafactura)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createPDF(
        nombreart: String,
        nombreproveedor: String?,
        importe: Float?,
        cantidad: Int?,
        fechafactura: String?
    ) {
        val document = Document()

        try {
            val nombreArchivo = "factura_${nombreproveedor?.replace("/", "_")}.pdf"
            val rutaCarpetaDescargas =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val rutaArchivo = File(rutaCarpetaDescargas, nombreArchivo)

            val writer = PdfWriter.getInstance(document, FileOutputStream(rutaArchivo))
            document.open()

            val baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
            val titleFont = Font(baseFont, 18f, Font.BOLD)
            val labelFont = Font(baseFont, 12f, Font.BOLD)
            val valueFont = Font(baseFont, 12f, Font.NORMAL)

            document.add(Paragraph(" ", valueFont))

            val titleParagraph = Paragraph("Factura de Compra", titleFont)
            titleParagraph.spacingAfter = 10f // Add spacing after the title
            document.add(titleParagraph)


            document.add(Paragraph("Artículo: ", labelFont))
            document.add(Paragraph(nombreart, valueFont))
            document.add(Paragraph(" ", valueFont))

            document.add(Paragraph("Proveedor: ", labelFont))
            document.add(Paragraph(nombreproveedor, valueFont))
            document.add(Paragraph(" ", valueFont))

            document.add(Paragraph("Importe: ", labelFont))
            document.add(Paragraph(importe.toString(), valueFont))
            document.add(Paragraph(" ", valueFont))

            document.add(Paragraph("Cantidad: ", labelFont))
            document.add(Paragraph(cantidad.toString(), valueFont))
            document.add(Paragraph(" ", valueFont))

            document.add(Paragraph("Fecha: ", labelFont))
            document.add(Paragraph(fechafactura, valueFont))
            document.add(Paragraph(" ", valueFont))

            document.close()

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val contentResolver = requireContext().contentResolver
            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    rutaArchivo.inputStream().copyTo(outputStream)
                }
            }

            Toast.makeText(
                requireContext(),
                "Factura impresa como $nombreArchivo",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al imprimir la factura", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
