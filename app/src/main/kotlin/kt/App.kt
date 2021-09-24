package kt
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.gson.*
import java.io.FileInputStream

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) { gson() }

    var db = initFirebase()

    routing {
        get("/hi") {
            call.respond(db.path("/").get<Any>())
        }
    }
}

fun initFirebase(): FirebaseDatabase {
    var serviceAccount = FileInputStream("app/src/main/resources/firebase.json")
    var databaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://coffee-project-2f340-default-rtdb.firebaseio.com/")
        .build()

    FirebaseApp.initializeApp(databaseOptions)
    return FirebaseDatabase.getInstance()
}