import android.content.Context
import com.example.hotelapp.api.HotelService
import com.example.hotelapp.classes.User
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.utils.SessionManager

object UserHolder {
    var currentUser: User? = null
    var favoriteHotels: MutableList<HotelItem> = mutableListOf()
    private val apiService = RetrofitClient.retrofit.create(HotelService::class.java)
    private var sessionManager: SessionManager? = null
    private var hotelRepositoryInstance: HotelRepository? = null

    fun initialize(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context)
            hotelRepositoryInstance = HotelRepository(apiService, sessionManager!!)
        }
    }

    fun getSessionManager(): SessionManager {
        return sessionManager ?: throw IllegalStateException("SessionManager must be initialized before accessing it")

    }

    fun getHotelRepository(): HotelRepository {
        return hotelRepositoryInstance ?: throw IllegalStateException("HotelRepository not initialized")
    }
}
