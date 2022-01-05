package com.unionbankph.corporate.dao.data.gateway

import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.dao.data.form.AgreeTNCPrivacyForm
import com.unionbankph.corporate.dao.data.form.DaoForm
import com.unionbankph.corporate.dao.data.form.JumioVerificationForm
import com.unionbankph.corporate.dao.data.form.PersonalInformationStepFourForm
import com.unionbankph.corporate.dao.data.form.PersonalInformationStepOneForm
import com.unionbankph.corporate.dao.data.form.PersonalInformationStepThreeForm
import com.unionbankph.corporate.dao.data.form.PersonalInformationStepTwoForm
import com.unionbankph.corporate.dao.data.model.*
import com.unionbankph.corporate.dao.data.source.local.DaoCache
import com.unionbankph.corporate.dao.data.source.remote.DaoRemote
import com.unionbankph.corporate.dao.domain.form.DaoGetSignatoryForm
import com.unionbankph.corporate.dao.domain.form.ValidateNominatedUserForm
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class DaoGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val daoRemote: DaoRemote,
    private val daoCache: DaoCache
) : DaoGateway {

    override fun getDaoToken(): Single<Response<TokenDaoDto>> {
        return daoRemote.getDaoToken()
    }

    override fun getAccessToken(): Single<String> {
        return daoCache.getAccessToken()
    }

    override fun getReferenceNumber(): Single<String> {
        return daoCache.getReferenceNumber()
    }

    override fun clearDaoCache(): Completable {
        return daoCache.clearDaoCache()
    }

    override fun saveAccessToken(token: String?): Completable {
        return daoCache.saveAccessToken(token)
    }

    override fun saveReferenceNumber(referenceNumber: String?): Completable {
        return daoCache.saveReferenceNumber(referenceNumber)
    }

    override fun saveSignatoryDetails(signatoryDetail: SignatoryDetail): Completable {
        return daoCache.saveSignatoryDetails(signatoryDetail)
    }

    override fun getSignatoryDetailsFromCache(): Single<SignatoryDetail> {
        return daoCache.getSignatoryDetails()
    }

    override fun submitDao(
        userToken: String?,
        referenceNumber: String?,
        daoForm: DaoForm
    ): Single<Response<SubmitDaoDto>> {
        daoForm.referenceNumber = referenceNumber
        daoForm.userToken = userToken
        return daoRemote.submitDao(daoForm)
    }

    override fun daoAgreement(
        userToken: String?,
        referenceNumber: String?,
        agreeTNCPrivacyForm: AgreeTNCPrivacyForm
    ): Single<Response<Message>> {
        return daoRemote.daoAgreement(userToken, referenceNumber, agreeTNCPrivacyForm)
    }

    override fun submitPersonalInformationStepOne(
        userToken: String?,
        referenceNumber: String?,
        personalInformationStepOneForm: PersonalInformationStepOneForm
    ): Single<Response<Message>> {
        return daoRemote.submitPersonalInformationStepOne(
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
        return daoRemote.submitPersonalInformationStepTwo(
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
        return daoRemote.submitPersonalInformationStepThree(
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
        return daoRemote.submitPersonalInformationStepFour(
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
        return daoRemote.submitJumioVerification(userToken, referenceNumber, jumioVerificationForm)
    }

    override fun submitSignature(
        userToken: String?,
        referenceNumber: String?,
        requestBody: RequestBody
    ): Single<Message> {
        return daoRemote.submitSignature(userToken, referenceNumber, requestBody)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getSignatoryDetails(
        daoGetSignatoryForm: DaoGetSignatoryForm
    ): Single<Response<DaoDetailsDto>> {
        return daoRemote.getSignatoryDetails(daoGetSignatoryForm)
    }

    override fun getCities(
        userToken: String?,
        referenceNumber: String?,
        provinceCode: String
    ): Single<Response<MutableList<CityDto>>> {
        return daoRemote.getCities(userToken, referenceNumber, provinceCode)
    }

    override fun getCities(
        userToken: String,
        provinceCode: String
    ): Single<Response<CityDtoResponse>> {
        return daoRemote.getCities(userToken, provinceCode)
    }

    override fun getProvinces(
        userToken: String?,
        referenceNumber: String?
    ): Single<Response<MutableList<ProvinceDto>>> {
        return daoRemote.getProvinces(userToken, referenceNumber)
    }

    override fun getProvinces(
        userToken: String
    ): Single<Response<ProvincesDtoResponse>> {
        return daoRemote.getProvinces(userToken)
    }

    override fun getOccupations(
        userToken: String?,
        referenceNumber: String?,
        pageable: Pageable
    ): Single<Response<PagedDto<OccupationDto>>> {
        return daoRemote.getOccupations(userToken, referenceNumber, pageable)
    }

    override fun getCounties(
        userToken: String?,
        referenceNumber: String?
    ): Single<Response<MutableList<CountryDto>>> {
        return daoRemote.getCountries(userToken, referenceNumber)
    }

    override fun validateNominatedUser(
        accessToken: String,
        validateNominatedUserForm: ValidateNominatedUserForm
    ): Single<Response<ValidateNominatedUserDto>> {
        return daoRemote.validateNominatedUser(
            accessToken,
            validateNominatedUserForm
        )
    }

    override fun getListIds(
        userToken: String?
    ): Single<Response<MutableList<IdDto>>> {
        return daoRemote.getListIds(userToken)
    }
}
