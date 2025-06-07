package com.xrandolphx.elegantcommerce.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.xrandolphx.elegantcommerce.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        analytics = FirebaseAnalytics.getInstance(this)

        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, "LoginRegisterActivity")
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "LoginRegisterActivity")
        }
    }
}