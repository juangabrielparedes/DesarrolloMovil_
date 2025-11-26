package com.example.serviciocomputadoras.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviciocomputadoras.data.model.RepairOrder
import com.example.serviciocomputadoras.data.repository.RepairOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RepairOrderViewModel(private val repo: RepairOrderRepository = RepairOrderRepository()) : ViewModel() {

    private val _orderCreated = MutableStateFlow<String?>(null)
    val orderCreated: StateFlow<String?> = _orderCreated

    private val _invoiceCreated = MutableStateFlow<String?>(null)
    val invoiceCreated: StateFlow<String?> = _invoiceCreated

    fun createOrder(order: RepairOrder) = viewModelScope.launch {
        val id = repo.createRepairOrder(order)
        _orderCreated.value = id
    }

    fun generateInvoiceFromOrder(orderId: String) = viewModelScope.launch {
        val invoiceId = repo.generateInvoiceFromOrder(orderId)
        _invoiceCreated.value = invoiceId
    }

    fun attachCheckoutUrl(invoiceId: String, url: String) = viewModelScope.launch {
        repo.attachCheckoutUrlToInvoice(invoiceId, url)
    }
}
