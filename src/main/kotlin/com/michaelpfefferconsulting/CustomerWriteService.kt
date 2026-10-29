package com.michaelpfefferconsulting

import java.io.File

class CustomerWriteService {

    fun write(customer: Customer, file : File) {
        if(!file.exists()) {
            val csvHeader = "customer_name;customer_type;email_id"
            file.appendText(csvHeader)
        }
        file.appendText("\n")
        file.appendText("${customer.customerName};${customer.customerType};${customer.emailId}")
    }

}