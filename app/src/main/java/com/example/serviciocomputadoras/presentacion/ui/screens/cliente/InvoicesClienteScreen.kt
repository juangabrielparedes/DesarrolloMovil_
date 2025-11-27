package com.example.serviciocomputadoras.presentacion.ui.screens.cliente

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.example.serviciocomputadoras.presentacion.viewmodel.InvoicesViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "InvoicesClienteUI"

@RequiresApi(Build.VERSION_CODES.O)
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

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text(text = "Facturas", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(8.dp))
        /*if (debug.isNotBlank()) {
            Text(text = "DEBUG: $debug", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp))
        }*/

        if (invoices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay facturas")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(invoices) { invoice ->
                    Card(modifier = Modifier
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
                                Text(text = invoice.createdAt?.let { formatTimestamp(it.seconds * 1000) } ?: "", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (invoice.status == "pending") {
                                    Column {
                                        Button(onClick = {
                                            val url = invoice.checkoutUrl
                                            if (!url.isNullOrBlank()) {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Log.w(TAG, "abrir checkout url failed: ${e.message}")
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.Payment, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pagar")
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedButton(onClick = {
                                            viewModel.markInvoiceAsPaid(invoice.invoiceId)
                                        }) {
                                            Text("Ya pagué", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                } else if (invoice.status == "paid") {
                                    Text(
                                        text = "✓ Pagado",
                                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                        style = MaterialTheme.typography.titleMedium
                                    )
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
                TextButton(onClick = {
                    // abrir checkout si existe
                    inv.checkoutUrl?.let { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.w(TAG, "abrir checkout url failed: ${e.message}")
                        }
                    }
                }) { Text("Pagar") }
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

@RequiresApi(Build.VERSION_CODES.O)
fun formatTimestamp(millis: Long): String {
    val zdt = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return zdt.format(fmt)
}

// Simple InvoiceDetailScreen (opcional ruta de detalle)
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
        // reuse the dialog content as a screen
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
                val context = LocalContext.current
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(invoice!!.checkoutUrl))
                    context.startActivity(intent)
                }) { Text("Pagar") }
            }
        }
    }
}
