package com.michaelpfefferconsulting

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureCustomerRouting() {
    routing {
        post("/customer") {
            try {
                val fileOutput = environment.config.property("file-output").getString()
                val customer = call.receive<Customer>()
                val customerWriteService = CustomerWriteService()
                customerWriteService.write(customer, fileOutput)
                call.respond(HttpStatusCode.OK, "Kunde erfolgreich angelegt")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}