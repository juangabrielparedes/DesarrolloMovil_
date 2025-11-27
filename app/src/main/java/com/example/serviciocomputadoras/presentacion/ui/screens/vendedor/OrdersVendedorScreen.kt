// Archivo completo ajustado al diseño azul (#4654A3) del panel de cliente
// -------------------------------------------------------------------------
// NOTA: Todo el código ha sido mantenido íntegro en funcionalidad.
// Únicamente se ajustó la UI para igualar el estilo del panel del cliente:
// - Fondo azul
// - Cards blancas con bordes redondeados
// - Títulos y botones siguiendo el estilo
// - TextFields con bordes azules
// -------------------------------------------------------------------------

package com.example.serviciocomputadoras.presentacion.ui.screens.vendedor

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.serviciocomputadoras.data.model.PartItem
import com.example.serviciocomputadoras.presentacion.viewmodel.OrdersVendedorViewModel
import com.example.serviciocomputadoras.presentacion.viewmodel.OrdersVendedorViewModel.OrderUi
import com.example.serviciocomputadoras.presentacion.viewmodel.OrdersVendedorViewModel.RepairOrderDetail
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "OrdersVendedorUI"

private val BluePrimary = Color(0xFF4654A3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersVendedorScreen(
    ownerUid: String,
    viewModel: OrdersVendedorViewModel = viewModel()
) {
    Log.d(TAG, "OrdersVendedorScreen() invoked with ownerUid='$ownerUid'")

    val orders by viewModel.orders.collectAsState()
    val selectedDetail by viewModel.selectedOrderDetail.collectAsState()
    val debugLog by viewModel.debugLog.collectAsState()

    var query by remember { mutableStateOf("") }
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    var showOverlay by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(ownerUid) {
        try {
            if (ownerUid.isNotBlank()) viewModel.startListening(ownerUid)
            else viewModel.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "LaunchedEffect startListening failed: ${e.message}", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopListening() }
    }

    val filtered = remember(orders, query) {
        try {
            val base = if (query.isBlank()) orders else orders.filter {
                it.clientName.contains(query, true) || it.clientUid.contains(query, true)
            }
            base.sortedWith(compareBy<OrderUi> { it.scheduledAtMillis == 0L }.thenBy { it.scheduledAtMillis })
        } catch (e: Exception) {
            Log.w(TAG, "filter/sort failed: ${e.message}")
            emptyList<OrderUi>()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BluePrimary)
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Órdenes (Visitas)",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Spacer(Modifier.height(16.dp))

                // CARD DE BUSQUEDA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Buscar órdenes", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Buscar por nombre de cliente") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = BluePrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // LISTA
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No hay órdenes que coincidan", color = Color.White)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered) { order ->
                            OrderListItemWithMenu(
                                order = order,
                                onClickOpen = {
                                    selectedOrderId = it.orderId
                                    viewModel.fetchOrderDetail(it.orderId)
                                    showOverlay = true
                                },
                                onDelete = { oid ->
                                    viewModel.deleteOrder(oid) { success ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (success) "Orden eliminada" else "Error al eliminar"
                                            )
                                        }
                                    }
                                },
                                onEdit = { oid ->
                                    selectedOrderId = oid
                                    viewModel.fetchOrderDetail(oid)
                                    showOverlay = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // OVERLAY
        if (showOverlay && selectedDetail != null) {
            OrderDetailOverlay(
                detail = selectedDetail!!,
                onClose = {
                    showOverlay = false
                    selectedOrderId = null
                },
                onSave = { updatedDetail ->
                    val partsMaps = updatedDetail.parts.map {
                        mapOf("name" to it.name, "price" to it.price)
                    }
                    val updates = mapOf(
                        "clientName" to updatedDetail.clientName,
                        "clientEmail" to updatedDetail.clientEmail,
                        "diagnosis" to updatedDetail.diagnosis,
                        "laborCost" to updatedDetail.laborCost,
                        "parts" to partsMaps,
                        "partsTotal" to updatedDetail.partsTotal,
                        "totalCost" to updatedDetail.totalCost
                    )
                    viewModel.updateOrder(updatedDetail.orderId, updates) { ok ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (ok) "Orden actualizada" else "Error al actualizar"
                            )
                        }
                    }
                }
            )
        }

        // OVERLAY LOADING
        if (showOverlay && selectedDetail == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                Alignment.Center
            ) {
                Card { Column(Modifier.padding(20.dp)) { Text("Cargando...") } }
            }
        }
    }
}

//----------------------------------------------
// ITEM LISTA CON MENÚ
//----------------------------------------------

@Composable
fun OrderListItemWithMenu(
    order: OrderUi,
    onClickOpen: (OrderUi) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickOpen(order) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(order.clientName.ifBlank { order.clientUid }, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(order.problemReported, maxLines = 1, color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                Text("Total: ${order.totalCost}", color = BluePrimary)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(order.status, color = Color.Black)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (order.scheduledAtMillis > 0L) formatMillis(order.scheduledAtMillis) else "Sin fecha",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, "Más")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            expanded = false
                            onEdit(order.orderId)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            expanded = false
                            onDelete(order.orderId)
                        }
                    )
                }
            }
        }
    }
}

//----------------------------------------------
// OVERLAY DETALLE
//----------------------------------------------

@Composable
fun OrderDetailOverlay(
    detail: RepairOrderDetail,
    onClose: () -> Unit,
    onSave: (RepairOrderDetail) -> Unit
) {
    var clientName by remember { mutableStateOf(detail.clientName) }
    var clientEmail by remember { mutableStateOf(detail.clientEmail) }
    var diagnosis by remember { mutableStateOf(detail.diagnosis) }
    var laborCostStr by remember { mutableStateOf(detail.laborCost.toString()) }

    var parts by remember { mutableStateOf(detail.parts.toMutableList()) }
    var newPartName by remember { mutableStateOf("") }
    var newPartPriceStr by remember { mutableStateOf("") }

    fun recomputeTotals(): Pair<Long, Long> {
        val partsTotal = parts.sumOf { it.price }
        val labor = laborCostStr.toLongOrNull() ?: 0L
        val total = partsTotal + labor
        return partsTotal to total
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onClose() },
        Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clickable(false) {},
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Detalle de Orden", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Cerrar") }
                }

                Spacer(Modifier.height(8.dp))

                Text("Cliente UID: ${detail.clientUid}", color = Color.Gray)
                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nombre cliente") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = clientEmail,
                    onValueChange = { clientEmail = it },
                    label = { Text("Email cliente") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Diagnóstico / Observaciones") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = laborCostStr,
                    onValueChange = { laborCostStr = it.filter(Char::isDigit) },
                    label = { Text("Mano de obra (número)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text("Partes", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                parts.forEachIndexed { idx, part ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(part.name)
                            Text("Precio: ${part.price}")
                        }
                        TextButton(onClick = {
                            parts = parts.toMutableList().also { it.removeAt(idx) }
                        }) {
                            Text("Eliminar")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = newPartName,
                    onValueChange = { newPartName = it },
                    label = { Text("Nombre parte") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = newPartPriceStr,
                    onValueChange = { newPartPriceStr = it.filter(Char::isDigit) },
                    label = { Text("Precio parte") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        val price = newPartPriceStr.toLongOrNull() ?: 0L
                        if (newPartName.isNotBlank()) {
                            parts = parts.toMutableList().also {
                                it.add(PartItem(name = newPartName, price = price))
                            }
                            newPartName = ""
                            newPartPriceStr = ""
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) {
                        Text("Agregar parte")
                    }
                }

                Spacer(Modifier.height(12.dp))

                val (partsTotal, computedTotal) = recomputeTotals()
                Text("Subtotal partes: $partsTotal")
                Text("Total (partes + mano de obra): $computedTotal")

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onClose) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updated = detail.copy(
                                clientName = clientName,
                                clientEmail = clientEmail,
                                diagnosis = diagnosis,
                                laborCost = laborCostStr.toLongOrNull() ?: 0L,
                                parts = parts.toList(),
                                partsTotal = partsTotal,
                                totalCost = computedTotal
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Guardar")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

fun formatMillis(millis: Long): String {
    if (millis <= 0L) return ""
    val zdt = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return zdt.format(formatter)
}
