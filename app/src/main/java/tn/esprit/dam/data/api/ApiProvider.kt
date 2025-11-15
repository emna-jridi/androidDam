// data/api/ApiProvider.kt
package tn.esprit.dam.data.api

import android.content.Context

object ApiProvider {
  //  const val BASE_URL = "http://10.0.2.2:3000/api/v1"

    //  Pour téléphone physique
    const val BASE_URL = "http://192.168.1.115:3000/api/v1"

    private var api: ShadowGuardApi? = null

    fun initialize(context: Context) {
        if (api == null) {
            api = ShadowGuardApi(context.applicationContext)
        }
    }

    fun getApi(): ShadowGuardApi {
        return api ?: throw IllegalStateException(
            "ApiProvider not initialized. Call ApiProvider.initialize(context) first"
        )
    }

    fun cleanup() {
        api?.close()
        api = null
    }
}