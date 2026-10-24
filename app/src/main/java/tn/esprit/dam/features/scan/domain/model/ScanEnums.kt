package tn.esprit.dam.features.scan.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AnalysisType {
    @SerialName("installed_app") INSTALLED_APP,
    @SerialName("apk_upload") APK_UPLOAD
}
