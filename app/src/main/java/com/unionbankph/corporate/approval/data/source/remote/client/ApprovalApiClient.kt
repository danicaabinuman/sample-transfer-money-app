package com.unionbankph.corporate.approval.data.source.remote.client

import com.unionbankph.corporate.approval.data.model.ApprovalForm
import com.unionbankph.corporate.approval.data.model.ApprovalHierarchyDto
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.data.model.CheckWriterActivityLogDto
import com.unionbankph.corporate.branch.data.model.BranchVisitDto
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by herald25santos on 2020-01-15
 */
interface ApprovalApiClient {

    @GET("api/{api_version}/transactions/for-approvals")
    fun getApprovalList(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Query("status")
        status: String,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int,
        @Query("filter")
        filter: String
    ): Single<Response<PagedDto<Transaction>>>

    @GET("api/{api_version}/transactions/approval-process/{id}/hierarchy")
    fun getApprovalHierarchy(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<ApprovalHierarchyDto>>

    @POST("api/{api_version}/transactions/approval")
    fun approval(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        approvalForm: ApprovalForm
    ): Single<Response<ApprovalForm>>

    @GET("api/{api_version}/transactions/checkwriter/{id}")
    fun getCheckWriterDetails(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<Transaction>>

    @GET("api/{api_version}/customers/queue/{id}")
    fun getCashWithdrawalDetails(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<BranchVisitDto>>

    @GET("api/{api_version}/transactions/checkwriter/{id}/logs")
    fun getCheckWriterActivityLogs(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Path("id")
        id: String
    ): Single<Response<MutableList<CheckWriterActivityLogDto>>>

}