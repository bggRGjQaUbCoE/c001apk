package com.example.coolapk.logic.network

import com.example.coolapk.logic.model.HomeFeedResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface HomeService {

    @GET("/v6/main/indexV8?ids=")
    @Headers(
        "X-Requested-With: XMLHttpRequest",
        "X-App-Id: com.coolapk.market",
        "X-App-Device: wMxASdvl1ciJGbv92QgsDM2gTOH1STTByOn5Wdz1WYzByOn5Wdz1WYzByO3AjO4UjOxkjOCNkOBZkO2kDI7AyOgsjYkRmZ4MmNxADN0YWYllDZ",
        "X-App-Token: v2JDJhJDEwJE1TNDJPVFl3TXpRNE1rVTUvN2M4MXVDTHMua2NyTWFEV09RbXJVUFZWSm5FTzlCU0ZVOS5T"
    )
    fun getHomeFeed(
        @Query("page") page: Int,
        @Query("firstLaunch") firstLaunch: Int,
    ): Call<HomeFeedResponse>

}