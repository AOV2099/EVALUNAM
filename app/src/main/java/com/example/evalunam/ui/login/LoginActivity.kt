package com.example.evalunam.ui.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.evalunam.R
import com.example.evalunam.data.repository.MyCookieManager
import com.example.evalunam.databinding.ActivityLoginBinding
import com.example.evalunam.util.Utils
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient

class LoginViewModelFactory(private val okHttpClient: OkHttpClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(okHttpClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LoginActivity : AppCompatActivity() {

    //private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var picasso: Picasso

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(MyCookieManager())
        .build()

    private val loginViewModel by viewModels<LoginViewModel> {
        LoginViewModelFactory(okHttpClient)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)



        picasso = Picasso.Builder(this)
            .downloader(OkHttp3Downloader(okHttpClient))
            .build()


        //loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)


        loginViewModel.loginState.observe( this) { loginState ->
            when (loginState) {
                is LoginViewModel.LoginState.Success -> {

                    Utils().showToast(this, "Inicio de sesión exitoso")
                }

                is LoginViewModel.LoginState.Error -> {
                    Utils().showToast(this, "Fallo en el inicio de sesión")
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            val boleta = binding.etBoleta.text.toString()
            val password = binding.etPassword.text.toString()
            val captcha = binding.etCaptcha.text.toString()
            loginViewModel.login(boleta, password, captcha)
        }


        loginViewModel.captchaImageUrl.observe(this) { captchaImageUrl ->
            picasso.load(captchaImageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(binding.ivCaptcha)
        }

        loginViewModel.loadPageAndExtractCaptchaUrl()


    }
}