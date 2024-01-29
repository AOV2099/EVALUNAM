package com.example.evalunam.util

import android.content.Context
import android.widget.Toast

class Utils {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}