package com.michaelpfefferconsulting

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class CustomerWriteService {

    fun write(customer: Customer, file : File) {
        val bufferedWriter = BufferedWriter(FileWriter(file))
        val bufferedReader = BufferedReader(FileReader(file))
        if(bufferedReader.lines().count() == 0L) {
            val csvHeader = "customer_name;customer_type;email_id"
            bufferedWriter.append(csvHeader)
            bufferedWriter.newLine()
            bufferedWriter.flush()
        }
        bufferedWriter.newLine()
        bufferedWriter.append("${customer.customerName};${customer.customerType};${customer.emailId}")
        bufferedWriter.flush()
        bufferedWriter.close()
    }

}