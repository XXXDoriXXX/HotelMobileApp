package com.example.hotelapp

import HotelItem
import HotelRepository
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hotelapp.Holder.UserHolder

import com.example.hotelapp.api.HotelService
import com.example.hotelapp.network.RetrofitClient
import com.example.hotelapp.utils.SessionManager

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemsList: RecyclerView = view.findViewById(R.id.itemsHotelList)
        itemsList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val hotelRepository = UserHolder.getHotelRepository()

        hotelRepository.getHotels(
            onResult = { hotels ->
                if (isAdded) {
                    itemsList.adapter = ItemsHotelAdapter(hotels, requireContext())
                }
            },
            onError = { error ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }





    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
