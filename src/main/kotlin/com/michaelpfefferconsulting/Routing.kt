package com.michaelpfefferconsulting

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureCustomerRouting() {
    routing {
        post("/customer") {
            try {
                val fileOutput = environment.config.property("file-output").getString()
                val customer = call.receive<Customer>()
                val customerWriteService = CustomerWriteService()
                val file = File(fileOutput)
                customerWriteService.write(customer, file)
                call.respond(HttpStatusCode.OK, "Kunde erfolgreich angelegt")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}