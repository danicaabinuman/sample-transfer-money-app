package com.unionbankph.corporate.feature.definition

import androidx.annotation.IntDef

@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    CameraMode.DEFAULT,
    CameraMode.NEW_PHOTO,
    CameraMode.ADD_PHOTO,
)
annotation class CameraMode {
    companion object {
        const val DEFAULT = 0
        const val NEW_PHOTO = 1
        const val ADD_PHOTO = 2
    }
}