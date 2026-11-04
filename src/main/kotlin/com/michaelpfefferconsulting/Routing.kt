package com.michaelpfefferconsulting

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.io.path.Path

fun Application.configureCustomerRouting() {
    routing {
        post("/customer") {
            val hmacSignatureKey = environment.config.property("hmac-signature-key").getString()
            val frappeWebhookSignature = call.request.headers["X-Frappe-Webhook-Signature"]
            if (!frappeWebhookSignature.isNullOrEmpty() && hmacSignatureKey.isNotBlank()) {
                val security = Security(HMAC())
                val jsonCustomer = call.receiveText()
                println(jsonCustomer)
                val signatureValid = security.checkSignature(frappeWebhookSignature, jsonCustomer, hmacSignatureKey)
                if (signatureValid) {
                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        val fileOutput = environment.config.property("file-output").getString()
                        val filePath = Path(fileOutput)
                        val csvFileCreatorService = CsvFileCreatorService(CsvHeaderCreatorService())
                        csvFileCreatorService.create(filePath)
                        val customer = Json.decodeFromString<Customer>(jsonCustomer)
                        val csvFileWriterService = CsvFileWriterService(CustomerWriteService())
                        csvFileWriterService.write(filePath.toFile(), customer)
                    }
                    call.respond(HttpStatusCode.OK, "Kunde erfolgreich angelegt")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}