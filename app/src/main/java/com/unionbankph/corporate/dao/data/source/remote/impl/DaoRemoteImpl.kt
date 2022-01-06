package com.unionbankph.corporate.dao.data.source.remote.impl

import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.dao.data.form.*
import com.unionbankph.corporate.dao.data.model.*
import com.unionbankph.corporate.dao.data.source.remote.DaoRemote
import com.unionbankph.corporate.dao.data.source.remote.client.DaoApiClient
import com.unionbankph.corporate.dao.domain.form.DaoGetSignatoryForm
import com.unionbankph.corporate.dao.domain.form.ValidateNominatedUserForm
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Created by herald25santos on 4/8/20
 */
class DaoRemoteImpl
@Inject
constructor(
    retrofit: Retrofit
) : DaoRemote {

    private val daoApiClient: DaoApiClient =
        retrofit.create(DaoApiClient::class.java)

    override fun getDaoToken(): Single<Response<TokenDaoDto>> {
        return daoApiClient.getDaoToken(BuildConfig.CLIENT_API_VERSION)
    }

    override fun submitDao(
        daoForm: DaoForm
    ): Single<Response<SubmitDaoDto>> {
        return daoApiClient.submitDao(
            BuildConfig.CLIENT_API_VERSION,
            daoForm
        )
    }

    override fun daoAgreement(
        userToken: String?,
        referenceNumber: String?,
        agreeTNCPrivacyForm: AgreeTNCPrivacyForm
    ): Single<Response<Message>> {
        return daoApiClient.daoAgreement(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber,
            agreeTNCPrivacyForm
        )
    }

    override fun submitPersonalInformationStepOne(
        userToken: String?,
        referenceNumber: String?,
        personalInformationStepOneForm: PersonalInformationStepOneForm
    ): Single<Response<Message>> {
        return daoApiClient.submitPersonalInformationStepOne(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber,
            personalInformationStepOneForm
        )
    }

    override fun submitPersonalInformationStepTwo(
        userToken: String?,
        referenceNumber: String?,
        personalInformationStepTwoForm: PersonalInformationStepTwoForm
    ): Single<Response<Message>> {
        return daoApiClient.submitPersonalInformationStepTwo(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber,
            personalInformationStepTwoForm
        )
    }

    override fun submitPersonalInformationStepThree(
        userToken: String?,
        referenceNumber: String?,
        personalInformationStepThreeForm: PersonalInformationStepThreeForm
    ): Single<Response<Message>> {
        return daoApiClient.submitPersonalInformationStepThree(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber,
            personalInformationStepThreeForm
        )
    }

    override fun submitPersonalInformationStepFour(
        userToken: String?,
        referenceNumber: String?,
        personalInformationStepFourForm: PersonalInformationStepFourForm
    ): Single<Response<Message>> {
        return daoApiClient.submitPersonalInformationStepFour(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber,
            personalInformationStepFourForm
        )
    }

    override fun submitJumioVerification(
        userToken: String?,
        referenceNumber: String?,
        jumioVerificationForm: JumioVerificationForm
    ): Single<Response<Message>> {
        return daoApiClient.submitJumioVerification(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber,
            jumioVerificationForm
        )
    }

    override fun submitSignature(
        userToken: String?,
        referenceNumber: String?,
        requestBody: RequestBody
    ): Single<Response<Message>> {
        return daoApiClient.submitSignature(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber,
            requestBody
        )
    }

    override fun getSignatoryDetails(
        daoGetSignatoryForm: DaoGetSignatoryForm
    ): Single<Response<DaoDetailsDto>> {
        return daoApiClient.getSignatoryDetails(
            BuildConfig.CLIENT_API_VERSION,
            daoGetSignatoryForm
        )
    }

    override fun getCities(
        userToken: String?,
        referenceNumber: String?,
        provinceCode: String
    ): Single<Response<MutableList<CityDto>>> {
        return daoApiClient.getCities(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber,
            provinceCode
        )
    }

    override fun getCities(
        userToken: String,
        provinceCode: String
    ): Single<Response<CityDtoResponse>> {
        return daoApiClient.getCities(
            userToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION,
            provinceCode
        )
    }

    override fun getProvinces(
        userToken: String?,
        referenceNumber: String?
    ): Single<Response<MutableList<ProvinceDto>>> {
        return daoApiClient.getProvinces(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber
        )
    }

    override fun getProvinces(userToken: String): Single<Response<ProvincesDtoResponse>> {
        return daoApiClient.getProvinces(
            userToken,
            BuildConfig.MSME_CLIENT_ID,
            BuildConfig.MSME_CLIENT_SECRET,
            BuildConfig.MSME_CLIENT_API_VERSION
        )
    }

    override fun getOccupations(
        userToken: String?,
        referenceNumber: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<OccupationDto>>> {
        return daoApiClient.getOccupations(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber,
            pageable.page,
            pageable.size,
            pageable.filter
        )
    }

    override fun getCountries(
        userToken: String?,
        referenceNumber: String?
    ): Single<Response<MutableList<CountryDto>>> {
        return daoApiClient.getCountries(
            BuildConfig.CLIENT_API_VERSION,
            userToken,
            referenceNumber
        )
    }

    override fun validateNominatedUser(
        accessToken: String,
        validateNominatedUserForm: ValidateNominatedUserForm
    ): Single<Response<ValidateNominatedUserDto>> {
        return daoApiClient.validateNominatedUser(
            accessToken,
            BuildConfig.CLIENT_API_VERSION,
            validateNominatedUserForm
        )
    }

    override fun getListIds(
        userToken: String?
    ): Single<Response<MutableList<IdDto>>> {
        return daoApiClient.getListIds(
            BuildConfig.CLIENT_API_VERSION,
            userToken
        )
    }
}
