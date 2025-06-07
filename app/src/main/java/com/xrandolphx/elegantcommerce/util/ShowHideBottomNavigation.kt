package com.xrandolphx.elegantcommerce.util

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xrandolphx.elegantcommerce.R
import com.xrandolphx.elegantcommerce.activity.MainActivity

fun Fragment.hideBottomNavigationView() {
    val bottomNavigationView =
        (activity as MainActivity).findViewById<BottomNavigationView>(
            R.id.bottomNavigation
        )
    bottomNavigationView.visibility = View.GONE
}

fun Fragment.showBottomNavigationView() {
    val bottomNavigationView =
        (activity as MainActivity).findViewById<BottomNavigationView>(
            R.id.bottomNavigation
        )
    bottomNavigationView.visibility = View.VISIBLE
}