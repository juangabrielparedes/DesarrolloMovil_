package com.example.serviciocomputadoras.presentacion.ui.screens.cliente

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.serviciocomputadoras.data.model.Invoice
import com.example.serviciocomputadoras.data.remote.CreateCheckoutForInvoiceRequest
import com.example.serviciocomputadoras.data.remote.CreateCheckoutForInvoiceResponse
import com.example.serviciocomputadoras.data.remote.RetrofitClient
import com.example.serviciocomputadoras.presentacion.viewmodel.InvoicesViewModel
import retrofit2.Call
import retrofit2.Response

private const val TAG = "InvoicesClienteUI"

@Composable
fun InvoicesClienteScreen(
    currentUid: String,
    onOpenInvoice: (String) -> Unit = {},
    viewModel: InvoicesViewModel = viewModel()
) {
    val invoices by viewModel.invoices.collectAsState()
    val debug by viewModel.debug.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(currentUid) {
        viewModel.startListening(currentUid)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopListening() }
    }

    var selected by remember { mutableStateOf<Invoice?>(null) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
    ) {
        Text(
            text = "Facturas",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        /* Si quieres debug:
        if (debug.isNotBlank()) {
            Text(text = "DEBUG: $debug", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp))
        }
        */

        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay facturas")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(invoices) { invoice ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { selected = invoice },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = invoice.clientName ?: "", style = MaterialTheme.typography.titleMedium)
                                Text(text = "Total: ${invoice.total}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = invoice.status ?: "", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = invoice.createdAt?.let { formatTimestamp(it.seconds * 1000) } ?: "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (invoice.status == "pending") {
                                    // Button que abre checkout o pide backend si no existe checkoutUrl
                                    InvoicePayButton(invoice = invoice)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // detalle en dialog
    if (selected != null) {
        val inv = selected!!
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = {
                // ConfirmButton: abre checkout o pide backend para generar la sesión
                TextButton(onClick = {
                    val existingUrl = inv.checkoutUrl
                    if (!existingUrl.isNullOrBlank()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(existingUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.w(TAG, "abrir checkout url failed: ${e.message}")
                        }
                    } else {
                        // Llamar al backend para generar session y URL
                        Log.d(TAG, "Pidiendo backend generar checkout para invoice ${inv.invoiceId}")
                        val call = RetrofitClient.instance.createCheckoutForInvoice(
                            CreateCheckoutForInvoiceRequest(invoiceId = inv.invoiceId)
                        )
                        call.enqueue(object : retrofit2.Callback<CreateCheckoutForInvoiceResponse> {
                            override fun onResponse(
                                call: Call<CreateCheckoutForInvoiceResponse>,
                                response: Response<CreateCheckoutForInvoiceResponse>
                            ) {
                                if (response.isSuccessful) {
                                    val sessionUrl = response.body()?.url
                                    if (!sessionUrl.isNullOrBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sessionUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Log.w(TAG, "abrir session url failed: ${e.message}")
                                        }
                                    } else {
                                        Log.w(TAG, "Backend devolvió sin URL")
                                    }
                                } else {
                                    Log.w(TAG, "Error creando checkout: ${response.errorBody()?.string()}")
                                }
                            }

                            override fun onFailure(call: Call<CreateCheckoutForInvoiceResponse>, t: Throwable) {
                                Log.e(TAG, "Fallo al crear checkout: ${t.message}")
                            }
                        })
                    }
                }) {
                    Text("Pagar")
                }
            },
            dismissButton = {
                TextButton(onClick = { selected = null }) { Text("Cerrar") }
            },
            title = { Text("Factura ${inv.invoiceId}") },
            text = {
                Column {
                    Text("Cliente: ${inv.clientName} (${inv.clientEmail})")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total: ${inv.total}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Estado: ${inv.status}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Items:")
                    inv.items.forEach { it ->
                        Text("- ${it.desc}: ${it.price}")
                    }
                }
            }
        )
    }
}

/**
 * Botón reutilizable para pagar una invoice.
 * Si checkoutUrl existe abre el navegador, si no llama al backend para generar la sesión.
 */
@Composable
private fun InvoicePayButton(invoice: Invoice) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }

    Button(
        onClick = {
            val existingUrl = invoice.checkoutUrl
            if (!existingUrl.isNullOrBlank()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(existingUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.w(TAG, "abrir checkout url failed: ${e.message}")
                }
            } else {
                // llamar al backend
                loading = true
                val call = RetrofitClient.instance.createCheckoutForInvoice(
                    CreateCheckoutForInvoiceRequest(invoiceId = invoice.invoiceId)
                )
                call.enqueue(object : retrofit2.Callback<CreateCheckoutForInvoiceResponse> {
                    override fun onResponse(
                        call: Call<CreateCheckoutForInvoiceResponse>,
                        response: Response<CreateCheckoutForInvoiceResponse>
                    ) {
                        loading = false
                        if (response.isSuccessful) {
                            val sessionUrl = response.body()?.url
                            if (!sessionUrl.isNullOrBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sessionUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.w(TAG, "abrir session url failed: ${e.message}")
                                }
                            } else {
                                Log.w(TAG, "Backend devolvió sin URL")
                            }
                        } else {
                            Log.w(TAG, "Error creando checkout: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<CreateCheckoutForInvoiceResponse>, t: Throwable) {
                        loading = false
                        Log.e(TAG, "Fallo al crear checkout: ${t.message}")
                    }
                })
            }
        },
        enabled = !loading
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generando...")
        } else {
            Icon(Icons.Default.Payment, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pagar")
        }
    }
}

/**
 * Muestra un detalle de la invoice como pantalla alternativa.
 */
@Composable
fun InvoiceDetailScreen(invoiceId: String, currentUid: String, viewModel: InvoicesViewModel = viewModel()) {
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    LaunchedEffect(invoiceId) {
        invoice = viewModel.getInvoice(invoiceId)
    }
    if (invoice == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val context = LocalContext.current
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Factura ${invoice!!.invoiceId}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Cliente: ${invoice!!.clientName} (${invoice!!.clientEmail})")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Total: ${invoice!!.total}")
            Spacer(modifier = Modifier.height(16.dp))
            invoice!!.items.forEach { it ->
                Text(text = "- ${it.desc}: ${it.price}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (invoice!!.status == "pending" && !invoice!!.checkoutUrl.isNullOrBlank()) {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(invoice!!.checkoutUrl))
                    context.startActivity(intent)
                }) { Text("Pagar") }
            } else if (invoice!!.status == "pending" && invoice!!.checkoutUrl.isNullOrBlank()) {
                // si no hay checkoutUrl, pedirlo al backend (opcional)
                Button(onClick = {
                    val call = RetrofitClient.instance.createCheckoutForInvoice(
                        CreateCheckoutForInvoiceRequest(invoiceId = invoice!!.invoiceId)
                    )
                    call.enqueue(object : retrofit2.Callback<CreateCheckoutForInvoiceResponse> {
                        override fun onResponse(
                            call: Call<CreateCheckoutForInvoiceResponse>,
                            response: Response<CreateCheckoutForInvoiceResponse>
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.url?.let { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            } else {
                                Log.w(TAG, "Error creando checkout: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<CreateCheckoutForInvoiceResponse>, t: Throwable) {
                            Log.e(TAG, "Fallo al crear checkout: ${t.message}")
                        }
                    })
                }) { Text("Generar y pagar") }
            }
        }
    }
}

/**
 * Formatea timestamp (ms) a string legible.
 */
fun formatTimestamp(millis: Long): String {
    val zdt = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault())
    val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return zdt.format(fmt)
}

