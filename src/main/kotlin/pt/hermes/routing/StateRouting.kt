package pt.hermes.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pt.hermes.storage.StorageService

fun Application.stateRouting(
    storage: StorageService
) {
    routing {
        route("/state") {
            post("/save") {
                storage.save()
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}