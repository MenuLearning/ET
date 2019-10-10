package com.example.s1.menuui

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*


class LoginActivity : AppCompatActivity() {
    val TAG = "MyMessage"
    lateinit var auth: FirebaseAuth
    var googleSignInClient: GoogleSignInClient? = null
    var FACEBOOK_LOGIN_CODE = 9002
    val GOOGLE_LOGIN_CODE = 9001
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        skip.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        // 페이스북 콜백 등록
        callbackManager = CallbackManager.Factory.create();

        // 구글 로그인 옵션
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //토큰요청
            .requestEmail() //이메일 요청
            .build() //코드 끝

        // 구글 로그인 클래스를 만듬
        googleSignInClient = GoogleSignIn.getClient(this, gso) // 구글 로그인하는 클래스
        //callbackManager = CallbackManager.Factory.create() // 초기

        // 구글 로그인 버튼 이벤트
        google_login_button.setOnClickListener { googleLogin() }

        // 페이스북
        facebook_login_button.setReadPermissions("email","public_profile")
        facebook_login_button.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                // ...
            }
        })
        // 페이스북 로그인 버튼 이벤트

    }
    // 로그인 성공 시 이동할 페이지
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(
                this,
                "Login 완료",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // 구글 로그인 코드
    fun googleLogin() {
        progress_bar.visibility = View.VISIBLE
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    // 구글 로그인 성공 했을 때 결과값이 넘어오는 함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 구글에서 승인된 정보를 가지고 오기
        if (requestCode == GOOGLE_LOGIN_CODE) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // 구글 로그인이 성공 했을 경우
                var account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
                // moveMainPage(auth?.currentUser) //유저아이디를 넘겨준다.
            } else {
                progress_bar.visibility = View.GONE
            }
        }

    }

    // 구글 로그인 성공시 토큰값을 파이어베이스로 넘겨주어서 계정을 생성하는 콛,
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    //다음 페이지 호출
                    moveMainPage(auth?.currentUser)
                }
            }
    }

    // 자동 로그인 설정
    override fun onStart() {
        super.onStart()
        // 자동 로그인 설정
        moveMainPage(auth?.currentUser)
    }

    // [START auth_with_facebook]
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        // [START_EXCLUDE silent]

        // [END_EXCLUDE]

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithFacebook:success")
                    val user = auth.currentUser

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithFacebook:failure", task.exception)
                    Toast.makeText(baseContext, "Facebook Login failed.",
                        Toast.LENGTH_SHORT).show()

                }
            }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
