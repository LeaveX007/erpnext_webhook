package com.michaelpfefferconsulting

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.path.Path

fun Application.configureCustomerRouting() {
    routing {
        post("/customer") {
            val scope = CoroutineScope(Dispatchers.IO)
            val customer = call.receive<Customer>()
            val fileOutput = environment.config.property("file-output").getString()
            scope.launch {
                val csvFileCreatorService = CsvFileCreatorService(
                    CustomerWriteService(),
                    CsvHeaderCreatorService(),
                )
                val filePath = Path(fileOutput)
                csvFileCreatorService.create(filePath, customer)
            }
            call.respond(HttpStatusCode.OK, "Kunde erfolgreich angelegt")
        }
    }
}