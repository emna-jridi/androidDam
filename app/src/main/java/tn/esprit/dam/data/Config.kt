package tn.esprit.dam.data

object Config {
    // Centralized backend root URL. Update this to match your local/backend address.
    // Use http://10.0.2.2:3000 for emulator, or your machine IP when testing on device.
    const val ROOT_URL: String = "http://10.206.164.76:3000"
    const val API_ROOT: String = "$ROOT_URL/api/v1"
    const val BASE_URL: String = ROOT_URL
}
