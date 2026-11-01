package com.michaelpfefferconsulting

import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists

class CsvFileCreatorService(
    private val customerWriteService: CustomerWriteService,
    private val csvHeaderCreatorService: CsvHeaderCreatorService
) {

    fun create(filePath: Path, customer: Customer) {
        if (!filePath.exists()) {
            filePath.createFile()
            csvHeaderCreatorService.create(filePath.toFile())
        }

        customerWriteService.write(customer, filePath.toFile())
    }

}