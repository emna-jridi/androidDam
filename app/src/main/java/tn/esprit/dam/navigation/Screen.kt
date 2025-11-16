package tn.esprit.dam.navigation

sealed class Screens(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object EmailVerification : Screen("verification/{email}") {
        fun createRoute(email: String) = "verification/$email"
    }
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Scan : Screen("scan") // ✅ AJOUTER
    object Search : Screen("search")
    object History : Screen("history")
    object TopApps : Screen("topApps")
    object AppDetails : Screen("appDetails/{packageName}") {
        fun createRoute(packageName: String) = "appDetails/$packageName"
    }
}