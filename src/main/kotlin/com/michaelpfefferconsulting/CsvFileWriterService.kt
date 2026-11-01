package com.michaelpfefferconsulting

import java.io.File

class CsvFileWriterService(
    private val customerWriteService: CustomerWriteService
) {

    fun write(file: File, customer: Customer) {
        customerWriteService.write(customer, file)
    }

}