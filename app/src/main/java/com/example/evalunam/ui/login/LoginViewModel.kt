package com.example.evalunam.ui.login

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.evalunam.data.model.UserData
import com.example.evalunam.data.repository.ApiService
import com.example.evalunam.data.repository.MyCookieManager
import com.example.evalunam.util.Utils
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Call

class LoginViewModel(private val okHttpClient: OkHttpClient) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState>
        get() = _loginState

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData>
        get() = _userData

    private val _captchaImageUrl = MutableLiveData<String>()
    val captchaImageUrl: LiveData<String>
        get() = _captchaImageUrl


    private val BASE_URL: String = "https://www.dgae-siae.unam.mx/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(ApiService::class.java)

    interface ApiService {
        @GET("lib/captcha.php")
        fun loadDgae(): Call<ResponseBody>

        @GET("/www_try.php")
        fun getScoreTable(): Call<ResponseBody>

        @GET("lib/captcha.php")
        fun fetchCaptcha(): Call<ResponseBody>

        @GET("www_gate.php")
        fun loadPageWithCaptcha(): Call<ResponseBody>

        @GET("mod_idn/www_idnt.php")
        fun getCurp(): Call<ResponseBody>

        @POST("www_gate.php")
        @FormUrlEncoded
        //@Headers("Content-Type: application/x-www-form-urlencoded")
        fun logIntoSiae(
            @Field("acc") acc: String,
            @Field("usr_logi") usrLogi: String,
            @Field("usr_pass") usrPass: String,
            @Field("captcha") captcha: String
        ): retrofit2.Call<Void>
    }

    @OptIn(DelicateCoroutinesApi::class)
     fun login(name: String, pass: String, captcha: String) {
         Log.d("LOGIN BTN", "LOGIN")
        try {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val requestBuilder = createLoginRequestBuilder(name, pass, captcha)
                    val request = requestBuilder.build()
                    val response = okHttpClient.newCall(request).execute()

                    withContext(Dispatchers.Main) {
                        handleLoginResponse(response)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun createLoginRequestBuilder(name: String, pass: String, captcha: String): Request.Builder {
        val formBody = createFormBody(name, pass, captcha)
        val headers = createHeaders()

        return Request.Builder()
            .url("https://www.dgae-siae.unam.mx/www_gate.php")
            .post(formBody)
            .headers(headers)
    }

    private fun createFormBody(name: String, pass: String, captcha: String): RequestBody {
        return FormBody.Builder()
            .add("acc", "aut")
            .add("usr_logi", name)
            .add("usr_pass", pass)
            .add("captcha", captcha)
            .build()
    }

    private fun createHeaders(): Headers {
        return Headers.Builder()
            .add("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
            .add("accept-language", "es-419,es;q=0.9")
            .build()
    }

    private fun handleLoginResponse(response: Response) {
        Log.d("LOGIN RESPONSE", "HANDLE LOGIN RESPONSE")
        if (response.isSuccessful) {
            val cookies = response.headers("Set-Cookie")

            if (response.body?.let { checkAccessSuccess(it.string()) } == true) {
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error("Credenciales incorrectas")
            }
        } else {
            // TODO: Manejar errores
        }
    }

    private fun checkAccessSuccess(htmlResponse: String): Boolean {
        return try {
            val doc = Jsoup.parse(htmlResponse)
            val titleElement = doc.select("title").first()
            val title = titleElement?.text()
            Log.d("LOGIN TITLE", title.toString())
            // Verificar si el título coincide con el esperado
            title?.contains("SIAE [Panel de Control]", ignoreCase = true) == true
        } catch (e: Exception) {
            e.printStackTrace()
            false // Retorna falso si ocurre una excepción
        }
    }

    /*private fun filterCookies() {
        val myCookieJar = okHttpClient.cookieJar as CookieManager

        // Imprimir cookies antes de limpiarlas
        val cookiesBeforeClear = myCookieJar.loadForRequest(BASE_URL.toHttpUrlOrNull()!!)
        for (cookie in cookiesBeforeClear) {
            Log.d("Cookie PHP Before Clear", "${cookie.name}: ${cookie.value}")
        }

        // Eliminar solo la primera cookie si hay más de una
        val cookies = myCookieJar.loadForRequest(BASE_URL.toHttpUrlOrNull()!!)
        if (cookies.size > 2) {
            val cookiesToKeep = cookies.drop(1) // Mantener todas excepto la primera
            myCookieJar.clearCookies()
            myCookieJar.saveFromResponse(BASE_URL.toHttpUrlOrNull()!!, cookiesToKeep)
        } else {
            // No hacer nada si solo hay una cookie o ninguna
        }
    }
*/
    @OptIn(DelicateCoroutinesApi::class)
    fun loadPageAndExtractCaptchaUrl() {
        try {
            Log.d("LOGIN", "CARGANDO PAGINA")

            // Limpiar cookies
            val myCookieJar = okHttpClient.cookieJar as MyCookieManager
            //val cookies = myCookieJar.loadForRequest(BASE_URL.toHttpUrlOrNull()!!)
            /*Log.d("LOGIN COOKIE jar", myCookieJar.toString())
            for (cookie in cookies) {
                Log.d("LOGIN COOKIES JAR", "${cookie.name} = ${cookie.value}")
            }*/
            myCookieJar.clearCookies()

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val call = apiService.loadPageWithCaptcha().execute()
                    Log.d("LOGIN CAPTCHA call", call.toString())

                    withContext(Dispatchers.Main) {
                        if (call.isSuccessful) {
                            // Imprime las cookies después de cargar la página
                            /*val myCookieJar = okHttpClient.cookieJar as MyCookieManager
                            val cookies = myCookieJar.loadForRequest(BASE_URL.toHttpUrlOrNull()!!)
                            for (cookie in cookies) {
                                Log.d("LOGIN COOKIES", "${cookie.name} = ${cookie.value}")
                            }*/

                            val htmlBody = call.body()?.string()
                            val captchaRelativeUrl = extractCaptchaRelativeUrl(htmlBody)

                            if (captchaRelativeUrl != null) {
                                val captchaFullUrl = BASE_URL.toHttpUrlOrNull()!!.newBuilder()
                                    .addPathSegments(captchaRelativeUrl)
                                    .build()

                                loadCaptchaImage(captchaFullUrl.toString())

                            } else {
                                Log.e("LOGIN CAPTCHA", "No se pudo extraer la URL de la imagen del captcha")
                            }
                        } else {
                            Log.e("LOGIN CAPTCHA", "No se pudo cargar la página")
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadCaptchaImage(imageUrl: String) {
        _captchaImageUrl.value = imageUrl
    }


    // Extraer la URL relativa de la imagen del captcha del HTML (usando JSoup)
    private fun extractCaptchaRelativeUrl(html: String?): String? {
        return try {
            val doc = Jsoup.parse(html)
            val captchaImg = doc.select("img.captcha").first()
            captchaImg?.attr("src") // Devuelve la URL relativa de la imagen del captcha
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    sealed class LoginState {
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }


}