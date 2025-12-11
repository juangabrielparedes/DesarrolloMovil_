package com.example.serviciocomputadoras.presentacion.ui.screens.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.serviciocomputadoras.data.model.Invoice

@Composable
fun EmbeddedCheckoutDialog(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onPaid: (Invoice) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var card by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current


    LaunchedEffect(loading) {
        if (loading) {

            delay(1200)
            val updated = invoice.copy(status = "paid", checkoutUrl = "embedded_checkout")
            onPaid(updated)
            loading = false
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("Pagar factura ${invoice.invoiceId}") },
        text = {
            Column {
                Text("Total: ${invoice.total}")
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre en la tarjeta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = card,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() || ch == ' ' }
                        if (filtered.length <= 19) card = filtered
                    },
                    label = { Text("Número de tarjeta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { if (it.length <= 5) expiry = it },
                        label = { Text("MM/AA") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = cvc,
                        onValueChange = {
                            val filtered = it.filter { ch -> ch.isDigit() }
                            if (filtered.length <= 4) cvc = filtered
                        },
                        label = { Text("CVC") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                error = null
                if (name.isBlank() || card.length < 12 || expiry.length < 3 || cvc.length < 3) {
                    error = "Completa los datos correctamente."
                    return@TextButton
                }

                focusManager.clearFocus()
                loading = true
            }, enabled = !loading) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Procesando...")
                } else {
                    Text("Pagar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!loading) onDismiss() }) { Text("Cancelar") }
        }
    )
}
