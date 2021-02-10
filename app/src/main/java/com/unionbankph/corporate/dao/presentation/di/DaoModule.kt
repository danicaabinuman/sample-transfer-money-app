package com.unionbankph.corporate.dao.presentation.di

import android.content.Context
import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.common.domain.executor.PostExecutionThread
import com.unionbankph.corporate.common.domain.executor.ThreadExecutor
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.dao.domain.gateway.DaoGateway
import com.unionbankph.corporate.dao.domain.interactor.*
import com.unionbankph.corporate.dao.domain.mapper.SignatoryMapper
import dagger.Module
import dagger.Provides

@Module
class DaoModule {

    @Provides
    @PerApplication
    fun getDaoToken(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): GenerateDaoAccessToken =
        GenerateDaoAccessToken(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun getSignatoryDetails(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway,
        signatoryMapper: SignatoryMapper
    ): GetSignatoryDetails =
        GetSignatoryDetails(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway,
            signatoryMapper
        )

    @Provides
    @PerApplication
    fun getSignatoryDetailsFromCache(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        daoGateway: DaoGateway
    ): GetSignatoryDetailsFromCache =
        GetSignatoryDetailsFromCache(
            threadExecutor,
            postExecutionThread,
            daoGateway
        )

    @Provides
    @PerApplication
    fun daoAgreement(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): DaoAgreement =
        DaoAgreement(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun saveReferenceNumber(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        daoGateway: DaoGateway
    ): SaveReferenceNumber =
        SaveReferenceNumber(
            threadExecutor,
            postExecutionThread,
            daoGateway
        )

    @Provides
    @PerApplication
    fun saveTokenDeepLink(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        daoGateway: DaoGateway
    ): SaveTokenDeepLink =
        SaveTokenDeepLink(
            threadExecutor,
            postExecutionThread,
            daoGateway
        )

    @Provides
    @PerApplication
    fun getCities(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): GetCities =
        GetCities(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun getCityById(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): GetCityById =
        GetCityById(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun getProvinces(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): GetProvinces =
        GetProvinces(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun getOccupations(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): GetOccupations =
        GetOccupations(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )


    @Provides
    @PerApplication
    fun getProvinceById(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): GetProvinceById =
        GetProvinceById(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun getCountries(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): GetCountries =
        GetCountries(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun submitDao(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway,
        context: Context
    ): SubmitDao =
        SubmitDao(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway,
            context
        )

    @Provides
    @PerApplication
    fun submitPersonalInformationStepOne(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): SubmitPersonalInformationStepOne =
        SubmitPersonalInformationStepOne(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun submitPersonalInformationStepTwo(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): SubmitPersonalInformationStepTwo =
        SubmitPersonalInformationStepTwo(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun submitPersonalInformationStepThree(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): SubmitPersonalInformationStepThree =
        SubmitPersonalInformationStepThree(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun submitPersonalInformationStepFour(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): SubmitPersonalInformationStepFour =
        SubmitPersonalInformationStepFour(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun submitJumioVerification(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): SubmitJumioVerification =
        SubmitJumioVerification(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun submitSignature(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): SubmitSignature =
        SubmitSignature(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun validateNominatedUser(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        responseProvider: ResponseProvider,
        daoGateway: DaoGateway
    ): ValidateNominatedUser =
        ValidateNominatedUser(
            threadExecutor,
            postExecutionThread,
            responseProvider,
            daoGateway
        )

    @Provides
    @PerApplication
    fun clearDaoCache(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread,
        daoGateway: DaoGateway
    ): ClearDaoCache =
        ClearDaoCache(
            threadExecutor,
            postExecutionThread,
            daoGateway
        )
}
