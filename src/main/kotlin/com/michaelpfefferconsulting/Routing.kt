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
            val frappeWebhookSignature = call.request.headers["X-Frappe-Webhook-Signature"]
            val hmacSignatureKey = environment.config.property("hmac-signature-key").getString()
            if (!frappeWebhookSignature.isNullOrEmpty() && hmacSignatureKey.isNotBlank()) {
                val jsonCustomer = call.receiveText()
                val security = Security()
                val signatureValid = security.checkSignature(
                    frappeWebhookSignature,
                    jsonCustomer,
                    hmacSignatureKey
                )
                if (signatureValid) {
                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        val fileOutput = environment.config.property("file-output").getString()
                        val csvFileCreatorService = CsvFileCreatorService(
                            CsvHeaderCreatorService(),
                        )
                        val filePath = Path(fileOutput)
                        csvFileCreatorService.create(filePath)
                        val csvFileWriterService = CsvFileWriterService(
                            CustomerWriteService()
                        )
                        val customer = call.receive<Customer>()
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