package com.example.serviciocomputadoras.presentacion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviciocomputadoras.data.model.PartItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrdersVendedorViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    private val TAG = "OrdersVendedorVM"
    private val ordersCol = firestore.collection("repairOrders")

    // UI-lite version used for the list
    data class OrderUi(
        val orderId: String = "",
        val businessId: String = "",
        val ownerId: String = "",
        val clientUid: String = "",
        val clientName: String = "",
        val problemReported: String = "",
        val totalCost: Long = 0L,
        val status: String = "",
        val scheduledAtMillis: Long = 0L
    )

    // Full detail representation loaded on demand
    data class RepairOrderDetail(
        val orderId: String = "",
        val businessId: String = "",
        val ownerId: String = "",
        val clientUid: String = "",
        val clientName: String = "",
        val clientEmail: String = "",
        val deviceType: String = "",
        val problemReported: String = "",
        val diagnosis: String = "",
        val laborCost: Long = 0L,
        val parts: List<PartItem> = emptyList(),
        val partsTotal: Long = 0L,
        val totalCost: Long = 0L,
        val status: String = "",
        val createdAtMillis: Long = 0L,
        val scheduledAtMillis: Long = 0L
    )

    private val _orders = MutableStateFlow<List<OrderUi>>(emptyList())
    val orders: StateFlow<List<OrderUi>> = _orders

    private val _selectedOrderDetail = MutableStateFlow<RepairOrderDetail?>(null)
    val selectedOrderDetail: StateFlow<RepairOrderDetail?> = _selectedOrderDetail

    private val _debugLog = MutableStateFlow<String>("")
    val debugLog: StateFlow<String> = _debugLog

    private var registration: ListenerRegistration? = null

    fun startListening(ownerUid: String) {
        try {
            stopListening()
            if (ownerUid.isBlank()) {
                Log.d(TAG, "startListening received empty ownerUid -> not listening")
                return
            }
            Log.d(TAG, "startListening ownerUid=$ownerUid")
            registration = ordersCol.whereEqualTo("ownerId", ownerUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "snapshot error: ${error.message}", error)
                        _orders.value = emptyList()
                        _debugLog.value = "snapshot error: ${error.message}"
                        return@addSnapshotListener
                    }
                    val docs = snapshot?.documents ?: emptyList()
                    val list = docs.mapNotNull { d ->
                        try {
                            val orderId = d.id
                            val clientUid = d.getString("clientUid") ?: ""
                            val clientName = d.getString("clientName") ?: ""
                            val problem = d.getString("problemReported") ?: ""
                            val total = d.getLong("totalCost") ?: 0L
                            val status = d.getString("status") ?: ""
                            val scheduled = d.getTimestamp("scheduledDate")
                            val scheduledMillis = scheduled?.toDate()?.time ?: 0L
                            OrderUi(
                                orderId = orderId,
                                businessId = d.getString("businessId") ?: "",
                                ownerId = d.getString("ownerId") ?: "",
                                clientUid = clientUid,
                                clientName = clientName,
                                problemReported = problem,
                                totalCost = total,
                                status = status,
                                scheduledAtMillis = scheduledMillis
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "map order failed for doc ${d.id}: ${e.message}")
                            null
                        }
                    }
                    Log.d(TAG, "snapshot mapped ${list.size} orders for owner=$ownerUid")
                    _orders.value = list
                    _debugLog.value = "mapped ${list.size} orders"
                }
        } catch (e: Exception) {
            Log.e(TAG, "startListening threw: ${e.message}", e)
        }
    }

    fun stopListening() {
        try {
            registration?.remove()
            registration = null
            Log.d(TAG, "stopListening called")
        } catch (e: Exception) {
            Log.w(TAG, "stopListening error: ${e.message}")
        }
    }

    /**
     * Fetch full repair order detail by id and expose it in selectedOrderDetail.
     */
    fun fetchOrderDetail(orderId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "fetchOrderDetail orderId=$orderId")
                val doc = ordersCol.document(orderId).get().await()
                if (!doc.exists()) {
                    Log.w(TAG, "fetchOrderDetail: doc not found $orderId")
                    _selectedOrderDetail.value = null
                    _debugLog.value = "Order not found: $orderId"
                    return@launch
                }

                val clientUid = doc.getString("clientUid") ?: ""
                val clientName = doc.getString("clientName") ?: ""
                val clientEmail = doc.getString("clientEmail") ?: ""
                val deviceType = doc.getString("deviceType") ?: ""
                val problemReported = doc.getString("problemReported") ?: ""
                val diagnosis = doc.getString("diagnosis") ?: ""
                val laborCost = doc.getLong("laborCost") ?: 0L
                val partsRaw = doc.get("parts") as? List<*>
                val parts = partsRaw?.mapNotNull { item ->
                    when (item) {
                        is Map<*, *> -> {
                            val name = item["name"] as? String ?: ""
                            val price = (item["price"] as? Number)?.toLong() ?: 0L
                            PartItem(name = name, price = price)
                        }
                        else -> null
                    }
                } ?: emptyList()
                val partsTotal = doc.getLong("partsTotal") ?: parts.sumOf { it.price }
                val totalCost = doc.getLong("totalCost") ?: partsTotal + laborCost
                val status = doc.getString("status") ?: ""
                val createdAt = (doc.getTimestamp("createdAt") ?: Timestamp.now()).toDate().time
                val scheduledAt = (doc.getTimestamp("scheduledDate") ?: Timestamp(0,0)).toDate().time

                val detail = RepairOrderDetail(
                    orderId = doc.id,
                    businessId = doc.getString("businessId") ?: "",
                    ownerId = doc.getString("ownerId") ?: "",
                    clientUid = clientUid,
                    clientName = clientName,
                    clientEmail = clientEmail,
                    deviceType = deviceType,
                    problemReported = problemReported,
                    diagnosis = diagnosis,
                    laborCost = laborCost,
                    parts = parts,
                    partsTotal = partsTotal,
                    totalCost = totalCost,
                    status = status,
                    createdAtMillis = createdAt,
                    scheduledAtMillis = scheduledAt
                )

                _selectedOrderDetail.value = detail
                _debugLog.value = "fetched detail for ${doc.id}"
            } catch (e: Exception) {
                Log.e(TAG, "fetchOrderDetail failed: ${e.message}", e)
                _debugLog.value = "fetchOrderDetail failed: ${e.message}"
                _selectedOrderDetail.value = null
            }
        }
    }

    /**
     * Delete an order document by id.
     */
    fun deleteOrder(orderId: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "deleteOrder orderId=$orderId")
                ordersCol.document(orderId).delete().await()
                Log.d(TAG, "deleteOrder success orderId=$orderId")
                _debugLog.value = "deleteOrder success $orderId"
                onComplete?.invoke(true)
            } catch (e: Exception) {
                Log.e(TAG, "deleteOrder failed: ${e.message}", e)
                _debugLog.value = "deleteOrder failed: ${e.message}"
                onComplete?.invoke(false)
            }
        }
    }

    /**
     * Update order with a map of fields.
     * For parts, pass list of maps [{name:..., price:...}, ...]
     */
    fun updateOrder(orderId: String, updates: Map<String, Any>, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "updateOrder orderId=$orderId updates=${updates.keys}")
                ordersCol.document(orderId).update(updates).await()
                Log.d(TAG, "updateOrder success orderId=$orderId")
                _debugLog.value = "updateOrder success $orderId"
                // refresh detail
                fetchOrderDetail(orderId)
                onComplete?.invoke(true)
            } catch (e: Exception) {
                Log.e(TAG, "updateOrder failed: ${e.message}", e)
                _debugLog.value = "updateOrder failed: ${e.message}"
                onComplete?.invoke(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        Log.d(TAG, "onCleared -> stopped listening")
    }
}
