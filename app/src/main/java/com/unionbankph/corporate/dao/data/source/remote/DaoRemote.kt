package com.unionbankph.corporate.dao.data.source.remote

import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.dao.data.form.AgreeTNCPrivacyForm
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.data.form.JumioVerificationForm
import com.unionbankph.corporate.dao.data.form.PersonalInformationStepFourForm
import com.unionbankph.corporate.dao.data.form.PersonalInformationStepOneForm
import com.unionbankph.corporate.dao.data.form.PersonalInformationStepThreeForm
import com.unionbankph.corporate.dao.data.form.PersonalInformationStepTwoForm
import com.unionbankph.corporate.dao.data.model.*
import com.unionbankph.corporate.dao.domain.form.DaoGetSignatoryForm
import com.unionbankph.corporate.dao.domain.form.ValidateNominatedUserForm
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.Response

interface DaoRemote {

    fun getDaoToken(): Single<Response<TokenDaoDto>>

    fun submitDao(
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
    ): Single<Response<Message>>

    fun getSignatoryDetails(
        daoGetSignatoryForm: DaoGetSignatoryForm
    ): Single<Response<DaoDetailsDto>>

    fun getCities(
        userToken: String?,
        referenceNumber: String?,
        provinceCode: String
    ): Single<Response<MutableList<CityDto>>>

    fun getCities(
        userToken: String,
        provinceCode: String
    ): Single<Response<CityDtoResponse>>

    fun getProvinces(
        userToken: String?,
        referenceNumber: String?
    ): Single<Response<MutableList<ProvinceDto>>>

    fun getProvinces(userToken: String): Single<Response<ProvincesDtoResponse>>

    fun getOccupations(
        userToken: String?,
        referenceNumber: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<OccupationDto>>>

    fun getCountries(
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
