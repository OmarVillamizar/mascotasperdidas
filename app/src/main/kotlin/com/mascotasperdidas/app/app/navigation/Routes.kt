package com.mascotasperdidas.app.app.navigation

sealed class Routes(val route: String) {

    // ── Auth / first-time setup ───────────────────────────────────────
    object Splash : Routes("splash")
    object Profile : Routes("profile") // first-time setup only
    object Otp : Routes("otp")
    object Permissions : Routes("permissions")

    // ── Main shell (bottom NavigationBar) ────────────────────────────
    object Main : Routes("main")
    object Feed : Routes("main/feed")
    object Map : Routes("main/map")
    object Notifications : Routes("main/notifications")
    object ProfileTab : Routes("main/profile") // profile inside main tabs

    // ── Stack from Main ───────────────────────────────────────────────
    object Settings : Routes("settings")
    object MyReports : Routes("my_reports")

    // ── Creation wizard ───────────────────────────────────────────────
    object NewReport : Routes("new_report")
    object FoundSubType : Routes("found_sub_type")
    object LostReportForm : Routes("lost_report_form")
    object SightingReportForm : Routes("sighting_report_form")
    object InCareReportForm : Routes("in_care_report_form")

    // ── Arg routes ────────────────────────────────────────────────────
    object ReportDetail : Routes("report_detail/{reportId}/{reportType}") {
        const val ARG_REPORT_ID = "reportId"
        const val ARG_REPORT_TYPE = "reportType"
        fun route(reportId: String, reportType: String) = "report_detail/$reportId/$reportType"
    }

    object ReportConfirmed : Routes("report_confirmed/{reportId}") {
        const val ARG_REPORT_ID = "reportId"
        fun route(reportId: String) = "report_confirmed/$reportId"
    }

    object SightingsForPet : Routes("sightings_for_pet/{petReportId}") {
        const val ARG_PET_REPORT_ID = "petReportId"
        fun route(petReportId: String) = "sightings_for_pet/$petReportId"
    }
}
