package com.michaelpfefferconsulting

import java.io.File

class CustomerWriteService {

    fun write(customer: Customer, file: File) {
        file.appendText("\n")
        file.appendText("${customer.customerName};${customer.customerType};${customer.emailId}")
    }

}