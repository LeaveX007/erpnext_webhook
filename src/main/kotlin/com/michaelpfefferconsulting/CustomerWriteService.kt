package com.michaelpfefferconsulting

import io.ktor.util.date.getTimeMillis
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class CustomerWriteService {

    fun write(customer: Customer, fileOutput : String) {
        val file = File("$fileOutput/${customer.customerName}_${getTimeMillis()}.csv")
        val bufferedWriter = BufferedWriter(FileWriter(file))
        val csvHeader = createCsvHeader()
        bufferedWriter.write(csvHeader)
        bufferedWriter.newLine()
        bufferedWriter.write("${customer.customerName};${customer.customerType};${customer.emailId}")
        bufferedWriter.flush()
    }

    private fun createCsvHeader(): String {
        return "customer_name;customer_type;email_id"
    }

}