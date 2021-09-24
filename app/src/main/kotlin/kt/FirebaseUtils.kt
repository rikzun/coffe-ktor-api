package kt
import com.google.firebase.database.*
import kotlinx.coroutines.CompletableDeferred

fun FirebaseDatabase.path(path: String): DatabaseReference {
    return this.getReferenceFromUrl("https://coffee-project-2f340-default-rtdb.firebaseio.com$path")
}

suspend fun <T>DatabaseReference.get(): T {
    val deferred = CompletableDeferred<T>()
    this.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            deferred.complete(snapshot.value as T)
        }

        override fun onCancelled(error: DatabaseError) {
            deferred.completeExceptionally(DatabaseException(error.message))
        }
    })
    return deferred.await()
}

suspend fun <T>DatabaseReference.set(value: T): T {
    val deferred = CompletableDeferred<T>()
    this.setValue(value) { _, _ ->
        deferred.complete(value)
    }
    return deferred.await()
}