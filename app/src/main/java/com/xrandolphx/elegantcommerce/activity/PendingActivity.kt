package com.xrandolphx.elegantcommerce.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xrandolphx.elegantcommerce.R

class PendingActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending)

        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = "Tu pago está pendiente. Recibirás una notificación cuando se confirme."
    }
}