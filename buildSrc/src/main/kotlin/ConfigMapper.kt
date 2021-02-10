fun getApplicationSuffixId(appId: AppId, env: Environment): String {
    return if (env == Environment.PRODUCTION) {
        if (appId == AppId.PORTAL) {
            ""
        } else {
            ".sme"
        }
    } else {
        if (appId == AppId.PORTAL) {
            ".${env.getLowerCaseName()}"
        } else {
            ".sme.${env.getLowerCaseName()}"
        }
    }

}

fun getVersionNameSuffix(env: Environment): String {
    return if (env == Environment.PRODUCTION) {
        ""
    } else {
        "-${env.getLowerCaseName()}"
    }
}

fun getAppName(appId: AppId, env: Environment): String {
    return if (env == Environment.PRODUCTION) {
        if (appId == AppId.PORTAL) {
            "UnionBank"
        } else {
            "UnionBank SME"
        }
    } else {
        if (appId == AppId.PORTAL) {
            "UnionBank ${env.getDisplayName()}"
        } else {
            "UnionBank SME ${env.getDisplayName()}"
        }
    }

}

fun getNewVersionCode(
    versionMajor: Int,
    versionMinor: Int,
    versionPatch: Int
) = AndroidSdk.min * 10000000 + versionMajor * 10000 + versionMinor * 100 + versionPatch
