package com.example.serviciocomputadoras.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("create-checkout-for-invoice")
    fun createCheckoutForInvoice(
        @Body body: CreateCheckoutForInvoiceRequest
    ): Call<CreateCheckoutForInvoiceResponse>

}
