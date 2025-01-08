import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "key_theme"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    fun applyTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val theme = prefs.getString(KEY_THEME, THEME_SYSTEM)

        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun saveTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme).apply()
        applyTheme(context)
    }
}
