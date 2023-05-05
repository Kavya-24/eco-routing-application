package com.example.ecoroute.ui.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoroute.R
import com.example.ecoroute.adapters.EVCarsListAdapter
import com.example.ecoroute.adapters.OnItemClickListener
import com.example.ecoroute.models.EVCar
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.UiUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_user.*
@Keep
@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")
class UserFragment : Fragment(), OnItemClickListener {

    private val TAG = UserFragment::class.java.simpleName
    private lateinit var root: View
    private val uiUtilInstance = UiUtils()
    private lateinit var pb: ProgressBar
    private val ctx = ApplicationUtils.getContext()

    override fun onResume() {
        super.onResume()
        val current_ev_cars = EVCarStorage.getCars(requireContext())
        checkOnStatus(current_ev_cars)
    }

    private fun checkOnStatus(currentEvCars: List<EVCar>) {
        if (currentEvCars.isEmpty()) {
            root.findViewById<ImageView>(R.id.img_no_car).visibility = View.VISIBLE
            root.findViewById<RecyclerView>(R.id.rv_ev_cars).visibility = View.GONE
        } else {
            loadCars(currentEvCars)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_user, container, false)


        root.findViewById<MaterialButton>(R.id.mtb_add_car).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_user_to_addCarFragment)
        }
        root.findViewById<ImageView>(R.id.info_user).setOnClickListener {
            Snackbar.make(
                csl_user,
                "You can add, display and delete your cars.",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        val current_ev_cars = EVCarStorage.getCars(requireContext())
        Log.e(TAG, "Cars = $current_ev_cars")
        checkOnStatus(current_ev_cars)


        return root
    }

    private fun loadCars(currentEvCars: List<EVCar>) {
        root.findViewById<ImageView>(R.id.img_no_car).visibility = View.GONE
        root.findViewById<RecyclerView>(R.id.rv_ev_cars).visibility = View.VISIBLE
        populate(currentEvCars)
    }

    private fun populate(currentEvCars: List<EVCar>) {
        val adapter = EVCarsListAdapter(this)
        val rv = root.findViewById<RecyclerView>(R.id.rv_ev_cars)
        adapter.lst = currentEvCars as MutableList<EVCar>
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun clickThisItem(_listItem: EVCar) {

    }
}
