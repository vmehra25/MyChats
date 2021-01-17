package com.example.mychats.auth

import com.example.mychats.R
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.example.mychats.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity(), View.OnClickListener {
    val TAG = "incident"
    val PHONE_NUMBER = "PHONE_NUMBER"
    var phoneNumber:String? = null
    var mVerificationId:String? = null
    var mResendToken:PhoneAuthProvider.ForceResendingToken? = null
    lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var auth = Firebase.auth
    private val mCounterDown:CountDownTimer? = null
    lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.mychats.R.layout.activity_otp)
        initViews()
        startVerify()
    }

    private fun startVerify() {
        startPhoneVerification()
        showTimer(60000)
        progressDialog = createProgressDialog("Sending a Verification Code", false)
        progressDialog.show()
    }

    private fun startPhoneVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber!!,
                60L,
                TimeUnit.SECONDS,
                this@OtpActivity,
                callbacks
        )
    }

    private fun showTimer(milliseconds: Long) {
        btnResentCode.isEnabled = false
        object:CountDownTimer(milliseconds, 1000){
            override fun onTick(millisUntilFinished: Long) {
                tvCounter.isVisible = true
                tvCounter.text = "Seconds Remaining: ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                tvCounter.isVisible = false
                btnResentCode.isEnabled = true
            }

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCounterDown?.cancel()
    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        tvVerify.text = getString(R.string.verify_number, phoneNumber)
        setSpannableStrings()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }

                val smsCode = credential.smsCode
                if(!smsCode.isNullOrBlank()){
                    etSentCode.setText(smsCode)
                }
                Log.d("incident", "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }
                Log.d("incident", "YES onVerificationFailed $e")
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
                notifyUserAndRetry("Your phone number might be wrong or check network connection")

            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                progressDialog.dismiss()
                tvCounter.isVisible = false
                mVerificationId = verificationId
                mResendToken = token
                // ...
            }
        }

        btnVerifyCode.setOnClickListener{
            val code = etSentCode.text.toString()
            Log.d("incident", "code: $code")
            if(!code.isNullOrEmpty() && !mVerificationId.isNullOrBlank()) {
                progressDialog = createProgressDialog("Please Wait...", false)
                progressDialog.show()
                val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                signInWithPhoneAuthCredential(credential)
            }
        }
        btnResentCode.setOnClickListener(this)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) {
                    if(it.isSuccessful){
                        startActivity(
                                Intent(this, SignUpActivity::class.java)
                        )
                        finish()
                        Log.d("incident", "success")
                    }else{
                        notifyUserAndRetry("Verification failed, try again")
                    }
                }
    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("OK"){_, _ ->
                showLoginActivity()
            }
            setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun setSpannableStrings() {
        val span = SpannableString(getString(R.string.sms_message, phoneNumber))
        val clickableSpan = object: ClickableSpan(){
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }

            override fun onClick(widget: View) {
                // send back to previous activity
                showLoginActivity()
            }
        }

        span.setSpan(clickableSpan, span.length-13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvWaiting.movementMethod = LinkMovementMethod.getInstance()
        tvWaiting.text = span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    override fun onBackPressed() {

    }

    override fun onClick(v: View) {
        when(v){

            btnResentCode -> {
                if(mResendToken != null){
                    showTimer(60000)
                    progressDialog = createProgressDialog("Sending verification code...", false)
                    progressDialog.show()
                    val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber!!)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(this)
                            .setCallbacks(callbacks)
                            .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            }
        }
    }
}


fun Context.createProgressDialog(message:String, isCancellable:Boolean):ProgressDialog{
    return ProgressDialog(this).apply {
        setCancelable(false)
        setMessage(message)
        setCanceledOnTouchOutside(false)
    }
}