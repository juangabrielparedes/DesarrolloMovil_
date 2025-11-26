package com.example.serviciocomputadoras.presentacion.ui.screens.vendedor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.serviciocomputadoras.data.model.PartItem
import com.example.serviciocomputadoras.data.model.RepairOrder
import com.example.serviciocomputadoras.presentacion.viewmodel.RepairOrderViewModel
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

@Composable
fun RepairOrderFormScreen(
    businessId: String,
    ownerId: String,
    clientUid: String,
    clientEmail: String,
    clientName: String,
    navController: NavController,
    viewModel: RepairOrderViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    var deviceType by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var laborCostText by remember { mutableStateOf("0") }
    var partName by remember { mutableStateOf("") }
    var partPriceText by remember { mutableStateOf("0") }
    var parts by remember { mutableStateOf(listOf<PartItem>()) }

    val orderCreated by viewModel.orderCreated.collectAsState()
    val invoiceCreated by viewModel.invoiceCreated.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Text(
            text = "Crear Orden de Reparación",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = clientName,
            onValueChange = { /* no editable */ },
            label = { Text("Cliente") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = clientEmail,
            onValueChange = { /* no editable */ },
            label = { Text("Email") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = deviceType,
            onValueChange = { deviceType = it },
            label = { Text("Tipo de equipo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = problem,
            onValueChange = { problem = it },
            label = { Text("Problema reportado") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = diagnosis,
            onValueChange = { diagnosis = it },
            label = { Text("Diagnóstico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Campo numérico: filtro manual para permitir solo dígitos (sin KeyboardOptions)
        OutlinedTextField(
            value = laborCostText,
            onValueChange = { laborCostText = it.filter { c -> c.isDigit() } },
            label = { Text("Costo mano de obra") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Text("Repuestos", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = partName,
                onValueChange = { partName = it },
                label = { Text("Nombre repuesto") },
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = partPriceText,
                onValueChange = { partPriceText = it.filter { c -> c.isDigit() } }, // filtro manual
                label = { Text("Valor") },
                modifier = Modifier.width(120.dp)
            )

            Spacer(Modifier.width(8.dp))

            Button(onClick = {
                if (partName.isNotBlank() && partPriceText.isNotBlank()) {
                    val new = parts.toMutableList()
                    new.add(PartItem(partName, partPriceText.toLong()))
                    parts = new
                    partName = ""
                    partPriceText = "0"
                }
            }) {
                Text("Agregar")
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(parts) { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(p.name)
                    Text("${p.price}")
                }
            }
        }

        val labor = laborCostText.toLongOrNull() ?: 0L
        val partsTotal = parts.sumOf { it.price }
        val total = labor + partsTotal

        Spacer(Modifier.height(8.dp))

        Text("Total: $total", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(12.dp))

        Row {
            Button(onClick = {
                val order = RepairOrder(
                    orderId = "",
                    businessId = businessId,
                    ownerId = ownerId,
                    clientUid = clientUid,
                    clientName = clientName,
                    clientEmail = clientEmail,
                    deviceType = deviceType,
                    problemReported = problem,
                    diagnosis = diagnosis,
                    laborCost = labor,
                    parts = parts,
                    partsTotal = partsTotal,
                    totalCost = total,
                    status = "pending_approval",
                    createdAt = Timestamp.now()
                )
                scope.launch {
                    viewModel.createOrder(order)
                }
            }) {
                Text("Guardar Orden")
            }

            Spacer(Modifier.width(8.dp))

            Button(onClick = {
                orderCreated?.let { id ->
                    scope.launch {
                        viewModel.generateInvoiceFromOrder(id)
                    }
                }
            }) {
                Text("Generar Factura")
            }
        }

        Spacer(Modifier.height(12.dp))

        // observadores
        if (invoiceCreated != null) {
            Text("Factura creada: $invoiceCreated", color = Color.Green)
            // opción para crear sesión de Stripe vendrá por cloud function
        }
    }
}
