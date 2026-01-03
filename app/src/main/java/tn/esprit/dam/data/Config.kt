package tn.esprit.dam.data

import tn.esprit.dam.BuildConfig

/**
 * Configuration object that uses BuildConfig for dynamic values
 * Separate API roots for debug/release builds
 * - Debug: http://10.0.2.2:3000 (emulator) or local IP (device)
 * - Release: Production endpoint
 */
object Config {
    // Dynamic configuration from BuildConfig based on build type
    const val ROOT_URL: String = BuildConfig.BASE_ROOT
    const val API_ROOT: String = BuildConfig.API_ROOT
    const val BASE_URL: String = BuildConfig.BASE_ROOT
}

