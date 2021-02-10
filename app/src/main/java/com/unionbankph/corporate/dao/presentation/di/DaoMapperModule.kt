package com.unionbankph.corporate.dao.presentation.di

import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.dao.domain.mapper.SignatoryMapper
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import dagger.Module
import dagger.Provides

@Module
class DaoMapperModule {

    @Provides
    @PerApplication
    fun signatoryMapper(
        settingsGateway: SettingsGateway
    ): SignatoryMapper = SignatoryMapper(settingsGateway)

}
