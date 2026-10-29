package com.michaelpfefferconsulting

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    @SerialName("customer_name")
    val customerName: String,
    @SerialName("customer_type")
    val customerType: String,
    @SerialName("email_id")
    val emailId: String? = null
)