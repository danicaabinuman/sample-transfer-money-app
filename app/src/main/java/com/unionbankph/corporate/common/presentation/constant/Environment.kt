package com.unionbankph.corporate.common.presentation.constant

import com.unionbankph.corporate.BuildConfig
import javax.annotation.concurrent.ThreadSafe

/**
 * Created by herald25santos on 4/24/20
 */
class Environment {

    @ThreadSafe
    companion object {
        val DEV: Boolean =
            BuildConfig.FLAVOR.equals("Dev", true) || BuildConfig.FLAVOR.equals("SMEDev", true)
        val QAT: Boolean =
            BuildConfig.FLAVOR.equals("Qat", true) || BuildConfig.FLAVOR.equals("SMEQat", true)
        val QAT2: Boolean =
            BuildConfig.FLAVOR.equals("Qat2", true) || BuildConfig.FLAVOR.equals("SMEQat2", true)
        val QAT3: Boolean =
            BuildConfig.FLAVOR.equals("Qat3", true) || BuildConfig.FLAVOR.equals("SMEQat3", true)
        val STAGING: Boolean =
            BuildConfig.FLAVOR.equals("Staging", true) || BuildConfig.FLAVOR.equals(
                "SMEStaging",
                true
            )
        val PREPRODUCTION: Boolean = BuildConfig.FLAVOR.equals(
            "Preproduction",
            true
        ) || BuildConfig.FLAVOR.equals("SMEPreproduction", true)
        val PRODUCTION: Boolean = BuildConfig.FLAVOR.equals(
            "Production",
            true
        ) || BuildConfig.FLAVOR.equals("SMEProduction", true)
    }
}
