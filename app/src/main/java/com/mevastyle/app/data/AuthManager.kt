package com.mevastyle.app.data
import android.content.Context; import android.content.Intent
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException; import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth; import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider; import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AuthManager {
    // *** SOSTITUISCI con il tuo Web Client ID da Firebase Console ***
    const val WEB_CLIENT_ID = "1019559684452-as9a08427282kn49ctaimomnjcd5ikg1.apps.googleusercontent.com"
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    fun getGoogleSignInClient(ctx: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(WEB_CLIENT_ID).requestEmail().build()
        return GoogleSignIn.getClient(ctx, gso)
    }
    fun getSignInIntent(ctx: Context): Intent = getGoogleSignInClient(ctx).signInIntent
    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInAccount? =
        try { task.getResult(ApiException::class.java) } catch (_: Exception) { null }
    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): FirebaseUser? {
        val cred = GoogleAuthProvider.getCredential(account.idToken, null)
        return try { auth.signInWithCredential(cred).await(); auth.currentUser } catch (_: Exception) { null }
    }
    suspend fun isAdmin(): Boolean {
        val email = currentUser?.email ?: return false
        return try { db.collection("admins").document(email).get().await().exists() } catch (_: Exception) { false }
    }
    fun signOut(ctx: Context) { auth.signOut(); getGoogleSignInClient(ctx).signOut() }
}
