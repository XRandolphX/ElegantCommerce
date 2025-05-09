package com.xrandolphx.elegantcommerce.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xrandolphx.elegantcommerce.R

class FailureActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failure)

        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = "El pago falló. Intenta nuevamente o contacta soporte."
    }
}