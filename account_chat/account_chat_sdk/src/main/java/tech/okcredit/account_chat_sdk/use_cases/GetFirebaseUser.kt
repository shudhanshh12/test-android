package tech.okcredit.account_chat_sdk.use_cases

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetFirebaseUser @Inject constructor(private val firebaseAuth: Lazy<FirebaseAuth>) :
    UseCase<Unit, String> {

    private var authStateListener: AuthStateListener? = null

    override fun execute(req: Unit): Observable<Result<String>> {

        return Observable.create<Result<String>> { emitter ->
            authStateListener =
                AuthStateListener { it ->
                    it.currentUser?.let {
                        emitter.onNext(Result.Success(it.uid))
                    }
                }
            authStateListener?.let {
                firebaseAuth.get().addAuthStateListener(it)
            }
        }.doOnDispose {
            authStateListener?.let {
                firebaseAuth.get().removeAuthStateListener(it)
            }
        }
    }
}
