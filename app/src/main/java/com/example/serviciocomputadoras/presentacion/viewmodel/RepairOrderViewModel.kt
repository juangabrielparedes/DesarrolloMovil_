package com.example.serviciocomputadoras.presentacion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviciocomputadoras.data.model.PartItem
import com.example.serviciocomputadoras.data.model.RepairOrder
import com.example.serviciocomputadoras.data.repository.RepairOrderRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

private const val TAG = "RepairOrderVM"

data class PartItemUi(
    val name: String = "",
    val price: Long = 0L
)

data class RepairOrderUiState(
    val clientUid: String = "",
    val clientName: String = "",
    val clientEmail: String = "",
    val businessId: String = "",
    val ownerId: String = "",
    val deviceType: String = "",
    val problemReported: String = "",
    val diagnosis: String = "",
    val laborCost: Long = 0L,
    val parts: List<PartItemUi> = emptyList(),
    val scheduledMillis: Long = 0L, // NUEVO: fecha acordada en millis (0 = no seteada)
    val isSubmitting: Boolean = false,
    val resultOrderId: String? = null,
    val resultInvoiceId: String? = null,
    val errorMessage: String? = null
)

class RepairOrderViewModel(
    private val repo: RepairOrderRepository = RepairOrderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepairOrderUiState())
    val uiState: StateFlow<RepairOrderUiState> = _uiState

    // Cargar información inicial (clientUid, ownerId, businessId)
    fun initForChat(clientUid: String, ownerId: String, businessId: String) {
        _uiState.value = _uiState.value.copy(
            clientUid = clientUid,
            ownerId = ownerId,
            businessId = businessId
        )
        // cargar nombre/email automáticamente (asíncrono)
        viewModelScope.launch {
            try {
                val info = repo.getUserInfo(clientUid)
                if (info != null) {
                    _uiState.value = _uiState.value.copy(
                        clientName = info.first ?: "",
                        clientEmail = info.second ?: ""
                    )
                    Log.d(TAG, "initForChat loaded user info: name=${info.first} email=${info.second}")
                } else {
                    Log.d(TAG, "initForChat: no user info found for $clientUid")
                }
            } catch (e: Exception) {
                Log.w(TAG, "initForChat: error loading user info ${e.message}")
            }
        }
    }

    // UI setters
    fun setDeviceType(v: String) { _uiState.value = _uiState.value.copy(deviceType = v) }
    fun setProblemReported(v: String) { _uiState.value = _uiState.value.copy(problemReported = v) }
    fun setDiagnosis(v: String) { _uiState.value = _uiState.value.copy(diagnosis = v) }
    fun setLaborCost(v: Long) { _uiState.value = _uiState.value.copy(laborCost = v) }
    fun setScheduledMillis(millis: Long) { _uiState.value = _uiState.value.copy(scheduledMillis = millis) }

    fun addEmptyPart() {
        val new = _uiState.value.parts.toMutableList()
        new.add(PartItemUi())
        _uiState.value = _uiState.value.copy(parts = new)
    }

    fun updatePart(index: Int, name: String, price: Long) {
        val new = _uiState.value.parts.toMutableList()
        if (index in new.indices) {
            new[index] = PartItemUi(name, price)
            _uiState.value = _uiState.value.copy(parts = new)
        }
    }

    fun removePart(index: Int) {
        val new = _uiState.value.parts.toMutableList()
        if (index in new.indices) {
            new.removeAt(index)
            _uiState.value = _uiState.value.copy(parts = new)
        }
    }

    // Derived totals
    fun partsTotal(): Long = _uiState.value.parts.sumOf { it.price }
    fun grandTotal(): Long = partsTotal() + (_uiState.value.laborCost)

    // Submit: crea RepairOrder e Invoice (delegado al repo)
    fun submitOrder(onComplete: (Boolean, String?, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            try {
                // Intentar resolver nombre/email si están vacíos
                val stateBefore = _uiState.value
                if (stateBefore.clientName.isBlank() || stateBefore.clientEmail.isBlank()) {
                    try {
                        val info = repo.getUserInfo(stateBefore.clientUid)
                        if (info != null) {
                            _uiState.value = _uiState.value.copy(
                                clientName = info.first ?: stateBefore.clientName,
                                clientEmail = info.second ?: stateBefore.clientEmail
                            )
                            Log.d(TAG, "submitOrder resolved user info: ${info.first} / ${info.second}")
                        } else {
                            Log.d(TAG, "submitOrder: no pudo resolver user info para ${stateBefore.clientUid}")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "submitOrder: fallo al obtener user info previo a submit: ${e.message}")
                    }
                }

                val state = _uiState.value
                val parts = state.parts.map { PartItem(name = it.name, price = it.price) }

                // convertir scheduledMillis a Timestamp? si es >0
                val scheduledTimestamp = if (state.scheduledMillis > 0L) {
                    Timestamp(Instant.ofEpochMilli(state.scheduledMillis).toEpochMilli() / 1000, ((state.scheduledMillis % 1000).toInt()))
                    // Note: Firebase Timestamp(long seconds, int nanoseconds) requires secs/nanos; we'll use helper below instead
                } else {
                    null
                }

                // better way: construct Timestamp from millis via Timestamp(Instant.ofEpochMilli(...)) isn't direct,
                // but we can use Timestamp( Date(millis) ) - however to avoid extra imports, we'll directly use Timestamp.now() fallback.
                // For correctness, we'll set scheduledDate as Timestamp.fromDate(Date(state.scheduledMillis)) in repository when creating.

                val order = RepairOrder(
                    orderId = "",
                    businessId = state.businessId,
                    ownerId = state.ownerId,
                    clientUid = state.clientUid,
                    clientName = state.clientName,
                    clientEmail = state.clientEmail,
                    deviceType = state.deviceType,
                    problemReported = state.problemReported,
                    diagnosis = state.diagnosis,
                    laborCost = state.laborCost,
                    parts = parts,
                    partsTotal = parts.sumOf { it.price },
                    totalCost = parts.sumOf { it.price } + state.laborCost,
                    status = "pending_approval",
                    createdAt = null,
                    scheduledDate = null // we will let repo set scheduledDate from state.scheduledMillis
                )

                // Llamamos al repo pasando también scheduledMillis: implementado en repo
                val result = repo.createOrderAndInvoiceWithSchedule(order, state.scheduledMillis)
                if (result != null) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        resultOrderId = result.first,
                        resultInvoiceId = result.second
                    )
                    Log.d(TAG, "submitOrder success orderId=${result.first} invoiceId=${result.second}")
                    onComplete(true, result.first, result.second)
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = "Error creando orden")
                    onComplete(false, null, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "submitOrder exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = e.message)
                onComplete(false, null, null)
            }
        }
    }
}
