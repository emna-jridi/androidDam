package tn.esprit.dam.navigation


sealed class Screens(val route: String) {
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
  object Profile : Screens("profile")
  object Home : Screens("home")
  object Scan : Screens("scan")
  object ScanHistory : Screens("scan_history")
  object AppSearch : Screens("app_search")
  object AppDetails : Screens("app_details/{packageName}") {
    fun createRoute(packageName: String) = "app_details/$packageName"
  }
  object Vault : Screens("vault")
  object VaultAddPassword : Screens("vault_add_password")
}
