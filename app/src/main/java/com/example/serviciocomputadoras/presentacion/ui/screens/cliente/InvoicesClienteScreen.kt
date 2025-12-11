package com.example.serviciocomputadoras.presentacion.ui.screens.cliente

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.serviciocomputadoras.data.model.Invoice
import com.example.serviciocomputadoras.presentacion.viewmodel.InvoicesViewModel

@Composable
fun InvoicesClienteScreen(
    currentUid: String,
    onOpenInvoice: (String) -> Unit = {},
    viewModel: InvoicesViewModel = viewModel()
) {
    val invoices by viewModel.invoices.collectAsState()
    val debug by viewModel.debug.collectAsState()

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

                                    InvoicePayButton(invoice = invoice, onPaymentSuccess = { paidInv ->
                                        viewModel.markInvoicePaidLocally(paidInv.invoiceId)
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    if (selected != null) {
        val inv = selected!!
        var showEmbeddedDialog by remember { mutableStateOf(false) }

        if (showEmbeddedDialog && selected != null) {
            EmbeddedCheckoutDialog(
                invoice = selected!!,
                onDismiss = { showEmbeddedDialog = false },
                onPaid = { paidInv ->
                    viewModel.markInvoicePaidLocally(paidInv.invoiceId)
                    showEmbeddedDialog = false
                    selected = null
                }
            )
        }

        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = {
                TextButton(onClick = {
                    // FORZAR embebido
                    showEmbeddedDialog = true
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


@Composable
private fun InvoicePayButton(
    invoice: Invoice,
    onPaymentSuccess: (Invoice) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        EmbeddedCheckoutDialog(
            invoice = invoice,
            onDismiss = { showDialog = false },
            onPaid = { paidInvoice ->
                onPaymentSuccess(paidInvoice)
            }
        )
    }

    Button(
        onClick = {

            showDialog = true
        }
    ) {
        Icon(Icons.Default.Payment, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Pagar")
    }
}


@Composable
fun InvoiceDetailScreen(invoiceId: String, currentUid: String, viewModel: InvoicesViewModel = viewModel()) {
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var showEmbeddedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        invoice = viewModel.getInvoice(invoiceId)
    }
    if (invoice == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
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

            if (invoice!!.status == "pending") {
                Button(onClick = { showEmbeddedDialog = true }) { Text("Pagar") }

                if (showEmbeddedDialog) {
                    EmbeddedCheckoutDialog(
                        invoice = invoice!!,
                        onDismiss = { showEmbeddedDialog = false },
                        onPaid = { paidInv ->
                            viewModel.markInvoicePaidLocally(paidInv.invoiceId)
                            showEmbeddedDialog = false
                        }
                    )
                }
            }
        }
    }
}


fun formatTimestamp(millis: Long): String {
    val zdt = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault())
    val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return zdt.format(fmt)
}
