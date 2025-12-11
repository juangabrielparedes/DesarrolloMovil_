package com.example.serviciocomputadoras.presentacion.ui.screens.vendedor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.serviciocomputadoras.presentacion.viewmodel.PartItemUi
import com.example.serviciocomputadoras.presentacion.viewmodel.RepairOrderViewModel
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "RepairOrderFormUI"

@Composable
fun RepairOrderFormScreen(
    clientUid: String,
    ownerUid: String,
    businessId: String,
    onClose: () -> Unit,
    onCreated: (orderId: String?, invoiceId: String?) -> Unit = { _, _ -> },
    viewModel: RepairOrderViewModel = viewModel()
) {
    //Inicializar con datos conocidos
    LaunchedEffect(clientUid, ownerUid, businessId) {
        viewModel.initForChat(clientUid, ownerUid, businessId)
    }

    val state by viewModel.uiState.collectAsState()
    val scroll = rememberScrollState()
    val context = LocalContext.current

    // Local display state for scheduled date/time (shows formatted string)
    val scheduledMillis = state.scheduledMillis
    val scheduledDisplay = remember(scheduledMillis) {
        if (scheduledMillis > 0L) {
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            fmt.format(Date(scheduledMillis))
        } else {
            ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(12.dp)
    ) {
        // Header con cerrar (minimizar -> onClose)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Formulario de Orden", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = { onClose() }) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar/minimizar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campos autocompletados (cliente, email, uids)
        OutlinedTextField(
            value = state.clientName,
            onValueChange = { /* readonly */ },
            label = { Text("Cliente") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.clientEmail,
            onValueChange = { /* readonly */ },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campos visibles para el propietario (ownerId, businessId también pueden mostrarse)
        OutlinedTextField(
            value = state.businessId,
            onValueChange = { },
            label = { Text("Business ID") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.ownerId,
            onValueChange = {},
            label = { Text("Owner ID") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Device type
        OutlinedTextField(
            value = state.deviceType,
            onValueChange = { viewModel.setDeviceType(it) },
            label = { Text("Tipo de dispositivo (ej. Laptop, Desktop)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Problem reported
        OutlinedTextField(
            value = state.problemReported,
            onValueChange = { viewModel.setProblemReported(it) },
            label = { Text("Problema reportado por el cliente") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Diagnosis
        OutlinedTextField(
            value = state.diagnosis,
            onValueChange = { viewModel.setDiagnosis(it) },
            label = { Text("Diagnóstico (opcional)") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scheduled Date/Time selector
        Text(text = "Fecha y hora acordada para la visita", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = scheduledDisplay,
                onValueChange = { /* readonly, use pickers */ },
                label = { Text("Fecha y hora") },
                modifier = Modifier.weight(1f),
                enabled = false,
                readOnly = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                // Mostrar DatePicker, luego TimePicker y guardar en viewModel
                val now = Calendar.getInstance()
                val dpd = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        // luego pedir hora
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.YEAR, year)
                        cal.set(Calendar.MONTH, month)
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                        val tpd = TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                cal.set(Calendar.MINUTE, minute)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                viewModel.setScheduledMillis(cal.timeInMillis)
                                Log.d(TAG, "Scheduled picked: ${cal.timeInMillis}")
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                        )
                        tpd.show()
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
                )
                dpd.datePicker.minDate = System.currentTimeMillis() - 1000 // evitar seleccionar fechas pasadas (opcional)
                dpd.show()
            }) {
                Text("Seleccionar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // botón para limpiar fecha
            if (scheduledMillis > 0L) {
                OutlinedButton(onClick = {
                    viewModel.setScheduledMillis(0L)
                }) {
                    Text("Quitar")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Labor cost
        OutlinedTextField(
            value = if (state.laborCost == 0L) "" else state.laborCost.toString(),
            onValueChange = {
                // Filtramos sólo dígitos y convertimos a Long
                val parsed = it.filter { ch -> ch.isDigit() }
                val v = if (parsed.isBlank()) 0L else parsed.toLong()
                viewModel.setLaborCost(v)
            },
            label = { Text("Costo mano de obra (números)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Partes dinámicas
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Partes / Repuestos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.addEmptyPart() }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar parte")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lista de partes (editable)
        state.parts.forEachIndexed { idx, p ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = p.name,
                    onValueChange = { viewModel.updatePart(idx, it, p.price) },
                    label = { Text("Nombre parte") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = if (p.price == 0L) "" else p.price.toString(),
                    onValueChange = {
                        val parsed = it.filter { ch -> ch.isDigit() }
                        val pr = if (parsed.isBlank()) 0L else parsed.toLong()
                        viewModel.updatePart(idx, p.name, pr)
                    },
                    label = { Text("Precio") },
                    modifier = Modifier.width(120.dp)
                )
                IconButton(onClick = { viewModel.removePart(idx) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar parte")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Totales calculados en vivo
        val partsTotal = viewModel.partsTotal()
        val grandTotal = viewModel.grandTotal()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Subtotal partes: $partsTotal")
            Text(text = "Mano obra: ${state.laborCost}")
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Total:", style = MaterialTheme.typography.titleMedium)
            Text(text = "$grandTotal", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botones: enviar / cancelar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { onClose() }) {
                Text("Cerrar")
            }

            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
            } else {
                Button(onClick = {
                    // Validaciones básicas
                    if (state.deviceType.isBlank()) {
                        Log.d(TAG, "Validación: deviceType vacío")
                        return@Button
                    }
                    if (state.problemReported.isBlank()) {
                        Log.d(TAG, "Validación: problemReported vacío")
                        return@Button
                    }

                    viewModel.submitOrder { success, orderId, invoiceId ->
                        if (success) {
                            Log.d(TAG, "Order creada orderId=$orderId invoiceId=$invoiceId")
                            onCreated(orderId, invoiceId)
                            onClose()
                        } else {
                            Log.w(TAG, "Fallo creando order")
                        }
                    }
                }) {
                    Text("Crear orden y factura")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

