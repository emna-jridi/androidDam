package tn.esprit.dam.navigation


sealed class Screens(val route: String) {

  // AUTH
  object Login : Screens("login")
  object Register : Screens("register")
  object EmailVerification : Screens("email_verification/{email}") {
    fun createRoute(email: String) = "email_verification/$email"
  }
  object VerificationSuccess : Screens("verification_success")
  object ForgotPassword : Screens("forgot_password")
  object ResetPasswordOTP : Screens("reset_password_otp/{email}") {
    fun createRoute(email: String) = "reset_password_otp/$email"
  }
  object NewPassword : Screens("new_password")
  object PasswordResetSuccess : Screens("password_reset_success")

  // HOME
  object Home : Screens("home")
  object Profile : Screens("profile")
  object Scan : Screens("scan")
  object Search : Screens("search")
  object History : Screens("history")
  object TopApps : Screens("topApps")

  // SCAN HISTORY
  object ScanHistory : Screens("scan_history")

  object ScanDetail : Screens("scan_detail/{scanId}") {
    fun createRoute(scanId: String) = "scan_detail/$scanId"
  }

  object ScanComparison : Screens("scan_comparison/{scanId1}/{scanId2}") {
    fun createRoute(scanId1: String, scanId2: String) =
      "scan_comparison/$scanId1/$scanId2"
  }

  // APP DETAILS
  object AppDetails : Screens("appDetails/{packageName}") {
    fun createRoute(packageName: String) = "appDetails/$packageName"
  }
}
