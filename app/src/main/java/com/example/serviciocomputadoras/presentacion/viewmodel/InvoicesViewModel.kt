package com.example.serviciocomputadoras.presentacion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.serviciocomputadoras.data.model.Invoice
import com.example.serviciocomputadoras.data.repository.InvoicesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

private const val TAG = "InvoicesVM"

class InvoicesViewModel(
    private val repo: InvoicesRepository = InvoicesRepository()
) : ViewModel() {

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _debug = MutableStateFlow("")
    val debug: StateFlow<String> = _debug.asStateFlow()

    private var registration: ListenerRegistration? = null

    fun startListening(clientUid: String) {
        Log.d(TAG, "startListening: recibido clientUid=$clientUid")

        // ★★★★★ ARREGLO IMPORTANTE ★★★★★
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        val finalUid = clientUid.ifBlank { firebaseUid ?: "" }

        Log.d(TAG, "startListening: usando finalUid=$finalUid (fallback de FirebaseAuth=$firebaseUid)")

        registration?.remove()

        if (finalUid.isBlank()) {
            _debug.value = "UID vacío (ni pantalla ni FirebaseAuth)"
            _invoices.value = emptyList()
            return
        }

        registration = repo.listenInvoicesForClient(finalUid) { list ->
            _invoices.value = list
            _debug.value = "invoices=${list.size} uid=$finalUid"
        }
    }

    fun stopListening() {
        registration?.remove()
        registration = null
        Log.d(TAG, "stopListening invoices")
    }

    // one-shot fetch
    fun fetchOnce(clientUid: String) {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        val finalUid = clientUid.ifBlank { firebaseUid ?: "" }

        Log.d(TAG, "fetchOnce usando UID=$finalUid")

        try {
            val res = runBlocking { repo.fetchInvoicesOnce(finalUid) }
            _invoices.value = res
        } catch (e: Exception) {
            Log.w(TAG, "fetchOnce failed: ${e.message}")
        }
    }

    suspend fun getInvoice(invoiceId: String): Invoice? {
        return try {
            repo.getInvoice(invoiceId)
        } catch (e: Exception) {
            Log.w(TAG, "getInvoice failed: ${e.message}")
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
