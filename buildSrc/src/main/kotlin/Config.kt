import java.util.*

object AndroidSdk {
    const val min = 21
    const val compile = 31
    const val target = compile
    const val dimension = "version"
    const val applicationId = "com.unionbankph.corporate"
    const val versionCode = 1
    const val versionName = "1.0"
    const val testRunner = "androidx.test.runner.AndroidJUnitRunner"
}

object Variable {
    const val STRING = "string"
    const val BOOLEAN = "boolean"
    const val INTEGER = "integer"
}

enum class VersionPatch {
    MAJOR, MINOR, PATCH;

    fun getDisplayName(): String {
        return this.name.toLowerCase(Locale.getDefault()).capitalize()
    }
}

enum class Environment {
    DEV,
    QAT,
    QAT2,
    QAT3,
    STAGING,
    PREPRODUCTION,
    PRODUCTION;

    fun getLowerCaseName(): String {
        return this.name.toLowerCase(Locale.getDefault())
    }

    fun getDisplayName(): String {
        return this.name.toLowerCase(Locale.getDefault()).capitalize()
    }

    fun getEnvironmentURLPrefix(): String {
        return when (this) {
            DEV -> "dev-"
            QAT -> "uat-"
            QAT2 -> "uat2-"
            QAT3 -> "uat3-"
            STAGING -> "staging-"
            PREPRODUCTION -> "preprod-"
            PRODUCTION -> ""
        }
    }

}

enum class AppId(val value: String) {
    SME("SME"),
    PORTAL("")
}

object Resources {
    val dirs = mutableListOf(
        "src/main/res/layouts/activities",
        "src/main/res/layouts/fragments",
        "src/main/res/layouts/headers",
        "src/main/res/layouts/items",
        "src/main/res/layouts/rows",
        "src/main/res/layouts/footers",
        "src/main/res/layouts/widgets",
        "src/main/res/layouts/dialogs",
        "src/main/res/layouts/views",
        "src/main/res/layouts/menus",
        "src/main/res/layouts/bottoms",
        "src/main/res/layouts/frames",
        "src/main/res/layouts",
        "src/main/res"
    )
    val dirsTest = mutableListOf(
        "src/test/resources"
    )
}
