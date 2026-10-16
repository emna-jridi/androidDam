package tn.esprit.dam.navigation

object Screens {
    object Login : Screen("login")
    object Register : Screen("register")
    object EmailVerification : Screen("email_verification/{email}") {
        fun createRoute(email: String) = "email_verification/$email"
    }
    object VerificationSuccess : Screen("verification_success")
    object ForgotPassword : Screen("forgot_password")
    object ResetPasswordOTP : Screen("reset_password_otp/{email}") {
        fun createRoute(email: String) = "reset_password_otp/$email"
    }
    object NewPassword : Screen("new_password/{email}") {
        fun createRoute(email: String) = "new_password/$email"
    }
    object PasswordResetSuccess : Screen("password_reset_success")

    object Home : Screen("home")
    object Profile : Screen("profile")
    object AlertsHistory : Screen("alerts_history")

    object Scan : Screen("scan")
    object ScanHistory : Screen("scan_history")
    object AppSearch : Screen("app_search")
    object AppDetails : Screen("app_details/{packageName}") {
        fun createRoute(packageName: String) = "app_details/$packageName"
    }

    object Vault : Screen("vault")
    object VaultDetail : Screen("vault_detail")
    object VaultAddPassword : Screen("vault_add_password")

    object DarkWebMonitoring : Screen("darkweb_monitoring")
    object BreachDetail : Screen("breach_detail/{breachId}") {
        fun createRoute(breachId: String) = "breach_detail/$breachId"
    }
}

open class Screen(val route: String)
