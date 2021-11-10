package com.unionbankph.corporate.dao.domain.gateway

import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.dao.data.form.*
import com.unionbankph.corporate.dao.data.model.*
import com.unionbankph.corporate.dao.domain.form.DaoGetSignatoryForm
import com.unionbankph.corporate.dao.domain.form.ValidateNominatedUserForm
import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.Response

interface DaoGateway {

    fun getDaoToken(): Single<Response<TokenDaoDto>>

    fun submitDao(
        userToken: String?,
        referenceNumber: String?,
        daoForm: DaoForm
    ): Single<Response<SubmitDaoDto>>

    fun daoAgreement(
        userToken: String?,
        referenceNumber: String?,
        agreeTNCPrivacyForm: AgreeTNCPrivacyForm
    ): Single<Response<Message>>

    fun submitPersonalInformationStepOne(
        userToken: String?,
        referenceNumber: String?,
        personalInformationStepOneForm: PersonalInformationStepOneForm
    ): Single<Response<Message>>

    fun submitPersonalInformationStepTwo(
        userToken: String?,
        referenceNumber: String?,
        personalInformationStepTwoForm: PersonalInformationStepTwoForm
    ): Single<Response<Message>>

    fun submitPersonalInformationStepThree(
        userToken: String?,
        referenceNumber: String?,
        personalInformationStepThreeForm: PersonalInformationStepThreeForm
    ): Single<Response<Message>>

    fun submitPersonalInformationStepFour(
        userToken: String?,
        referenceNumber: String?,
        personalInformationStepFourForm: PersonalInformationStepFourForm
    ): Single<Response<Message>>

    fun submitJumioVerification(
        userToken: String?,
        referenceNumber: String?,
        jumioVerificationForm: JumioVerificationForm
    ): Single<Response<Message>>

    fun submitSignature(
        userToken: String?,
        referenceNumber: String?,
        requestBody: RequestBody
    ): Single<Message>

    fun getSignatoryDetails(
        daoGetSignatoryForm: DaoGetSignatoryForm
    ): Single<Response<DaoDetailsDto>>

    fun getAccessToken(): Single<String>

    fun getReferenceNumber(): Single<String>

    fun clearDaoCache(): Completable

    fun saveAccessToken(token: String?): Completable

    fun saveReferenceNumber(referenceNumber: String?): Completable

    fun saveSignatoryDetails(signatoryDetail: SignatoryDetail): Completable

    fun getSignatoryDetailsFromCache(): Single<SignatoryDetail>

    fun getCities(
        userToken: String?,
        referenceNumber: String?,
        provinceCode: String
    ): Single<Response<MutableList<CityDto>>>

    fun getProvinces(
        userToken: String?,
        referenceNumber: String?
    ): Single<Response<MutableList<ProvinceDto>>>

    fun getOccupations(
        userToken: String?,
        referenceNumber: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<OccupationDto>>>

    fun getCounties(
        userToken: String?,
        referenceNumber: String?
    ): Single<Response<MutableList<CountryDto>>>

    fun validateNominatedUser(
        accessToken: String,
        validateNominatedUserForm: ValidateNominatedUserForm
    ): Single<Response<ValidateNominatedUserDto>>

    fun getListIds(
        userToken: String?
    ): Single<Response<MutableList<IdDto>>>
}
