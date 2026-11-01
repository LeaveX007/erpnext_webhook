package com.michaelpfefferconsulting

import java.io.File

class CsvHeaderCreatorService {

    fun create(file : File) {
        val csvHeader = "customer_name;customer_type;email_id"
        file.appendText(csvHeader)
    }

}