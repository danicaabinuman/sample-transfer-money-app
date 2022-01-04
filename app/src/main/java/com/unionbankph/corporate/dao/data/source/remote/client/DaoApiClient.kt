package com.unionbankph.corporate.dao.data.source.remote.client

import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.dao.data.form.*
import com.unionbankph.corporate.dao.data.model.*
import com.unionbankph.corporate.dao.domain.form.DaoGetSignatoryForm
import com.unionbankph.corporate.dao.domain.form.ValidateNominatedUserForm
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DaoApiClient {

    @POST("api/{api_version}/dao/token")
    fun getDaoToken(
        @Path("api_version")
        apiVersion: String
    ): Single<Response<TokenDaoDto>>

    @PUT("api/{api_version}/dao/applications/signatories")
    fun submitDao(
        @Path("api_version")
        apiVersion: String,
        @Body
        daoForm: DaoForm
    ): Single<Response<SubmitDaoDto>>

    @PUT("api/{api_version}/dao/applications/signatories/agreements")
    fun daoAgreement(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?,
        @Body
        agreeTNCPrivacyForm: AgreeTNCPrivacyForm
    ): Single<Response<Message>>

    @PUT("api/{api_version}/dao/applications/signatories")
    fun submitPersonalInformationStepOne(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?,
        @Body
        personalInformationStepOneForm: PersonalInformationStepOneForm
    ): Single<Response<Message>>

    @PUT("api/{api_version}/dao/applications/signatories")
    fun submitPersonalInformationStepTwo(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?,
        @Body
        personalInformationStepTwoForm: PersonalInformationStepTwoForm
    ): Single<Response<Message>>

    @PUT("api/{api_version}/dao/applications/signatories")
    fun submitPersonalInformationStepThree(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?,
        @Body
        personalInformationStepThreeForm: PersonalInformationStepThreeForm
    ): Single<Response<Message>>

    @PUT("api/{api_version}/dao/applications/signatories")
    fun submitPersonalInformationStepFour(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?,
        @Body
        personalInformationStepFourForm: PersonalInformationStepFourForm
    ): Single<Response<Message>>

    @PUT("api/{api_version}/dao/applications/signatories")
    fun submitJumioVerification(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?,
        @Body
        jumioVerificationForm: JumioVerificationForm
    ): Single<Response<Message>>

    @POST("api/{api_version}/dao/applications/signatories/signature")
    fun submitSignature(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?,
        @Body
        requestBody: RequestBody
    ): Single<Response<Message>>

    @POST("api/{api_version}/dao/applications/signatories")
    fun getSignatoryDetails(
        @Path("api_version")
        apiVersion: String,
        @Body
        daoGetSignatoryForm: DaoGetSignatoryForm
    ): Single<Response<DaoDetailsDto>>

    @GET("api/{api_version}/dao/cities")
    fun getCities(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?,
        @Query("provinceCode")
        provinceCode: String
    ): Single<Response<MutableList<CityDto>>>

    @GET("api/{api_version}/dao/provinces")
    fun getProvinces(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?
    ): Single<Response<MutableList<ProvinceDto>>>

    @GET("msme/api/cdao/{api_version}/provinces")
    fun getProvinces(
        @Header("Authorization")
        accessToken: String,
        @Header("x-client-id")
        clientId: String,
        @Header("x-client-secret")
        clientSecret: String,
        @Path("api_version")
        apiVersion: String
    ): Single<Response<ProvincesDtoResponse>>

    @GET("api/{api_version}/dao/occupations")
    fun getOccupations(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?,
        @Query("page")
        page: Int,
        @Query("size")
        size: Int,
        @Query("name")
        name: String?
    ): Single<Response<PagedDto<OccupationDto>>>

    @GET("api/{api_version}/dao/countries")
    fun getCountries(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?,
        @Query("referenceNumber")
        referenceNumber: String?
    ): Single<Response<MutableList<CountryDto>>>

    @POST("api/{api_version}/online-enrollment/validate-nominated-users")
    fun validateNominatedUser(
        @Header("Authorization")
        accessToken: String,
        @Path("api_version")
        apiVersion: String,
        @Body
        validateNominatedUserForm: ValidateNominatedUserForm
    ): Single<Response<ValidateNominatedUserDto>>

    @GET("api/{api_version}/dao/applications/list-of-ids")
    fun getListIds(
        @Path("api_version")
        apiVersion: String,
        @Query("userToken")
        userToken: String?
    ): Single<Response<MutableList<IdDto>>>
}
