package com.example.mychats.auth

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.example.mychats.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    val PHONE_NUMBER = "PHONE_NUMBER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val progressDialog = createProgressDialog("Loading", false)
        progressDialog.show()
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null){
            showSignUpActivity()
            progressDialog.dismiss()
        }
        progressDialog.dismiss()


        etMobileNumber.addTextChangedListener {
            btnSubmit.isEnabled = !(it.isNullOrEmpty() || it.length < 10)
        }

        btnSubmit.setOnClickListener {
            checkNumber()
        }
    }

    private fun showSignUpActivity() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    private fun checkNumber() {
        val countryCode = ccp.selectedCountryCodeWithPlus
        val mobileNumber = countryCode + etMobileNumber.text.toString()
        notifyUser(mobileNumber)
    }

    private fun notifyUser(mobileNumber: String) {
        MaterialAlertDialogBuilder(this)
                .setMessage("We will be verifying the mobile number:" + etMobileNumber.text.toString() + "Is it OK or you want to edit?")
                .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                    showOTPActivity(mobileNumber);
                }
                .setNegativeButton("Edit") { dialog, _ ->
                    dialog.dismiss();
                }
                .setCancelable(false)
                .create()
                .show();
    }

    private fun showOTPActivity(mobileNumber: String) {
        startActivity(Intent(this, OtpActivity::class.java)
                .putExtra(PHONE_NUMBER, mobileNumber)
        )
        finish()

    }
}