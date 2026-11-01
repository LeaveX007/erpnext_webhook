package com.michaelpfefferconsulting

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.path.Path

fun Application.configureCustomerRouting() {
    routing {
        post("/customer") {
            val scope = CoroutineScope(Dispatchers.IO)
            val frappeWebhookSignature = call.request.headers["X-Frappe-Webhook-Signature"]
            val hmacSignatureKey = environment.config.property("hmac-signature-key").getString()
            if (!frappeWebhookSignature.isNullOrEmpty() && hmacSignatureKey.isNotBlank()) {
                val jsonCustomer = call.receiveText()
                val hmacSha256 = generateHmacSha256(jsonCustomer, hmacSignatureKey)
                val base64 = Base64.encode(hmacSha256.encodeToByteArray())
                if (frappeWebhookSignature.contentEquals(base64)) {
                    val customer = call.receive<Customer>()
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