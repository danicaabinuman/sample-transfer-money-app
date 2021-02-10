package com.unionbankph.corporate.branch.data.source.remote.client

import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.branch.data.model.BranchVisitSubmitDto
import com.unionbankph.corporate.branch.presentation.model.BranchVisitForm
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BranchVisitApiClient {

    @GET("api/{api_version}/customers/queue")
    fun getBranchVisits(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int,
        @Query("filter")
        filter: String?
    ): Single<Response<PagedDto<BranchVisitDto>>>

    @GET("api/{api_version}/customers/queue/{id}")
    fun getBranchVisit(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<BranchVisitDto>>

    @GET("api/{api_version}/branches")
    fun getBranches(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<MutableList<Branch>>>

    @POST("api/{api_version}/customers/queue")
    fun submitBranchVisitTransaction(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        branchVisitForm: BranchVisitForm
    ): Single<Response<BranchVisitSubmitDto>>
}
