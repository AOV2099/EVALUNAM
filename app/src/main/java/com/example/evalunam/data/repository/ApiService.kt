package com.example.evalunam.data.repository

import okhttp3.Call
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("lib/captcha.php")
    fun loadDgae()
    @GET("/www_try.php")
    fun getScoreTable()

    @GET("lib/captcha.php")
    fun fetchCaptcha()

    @GET("www_gate.php")
    fun loadPageWithCaptcha()

    @GET("mod_idn/www_idnt.php")
    fun getCurp()

    @POST("www_gate.php")
    @FormUrlEncoded
    //@Headers("Content-Type: application/x-www-form-urlencoded")
    fun logIntoSiae(
        @Field("acc") acc: String,
        @Field("usr_logi") usrLogi: String,
        @Field("usr_pass") usrPass: String,
        @Field("captcha") captcha: String
    )
}