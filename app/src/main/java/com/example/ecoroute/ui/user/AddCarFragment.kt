package com.example.ecoroute.ui.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ecoroute.R
import com.example.ecoroute.models.EVCar
import com.example.ecoroute.ui.home.HomeFragment
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.UiUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AddCarFragment : Fragment() {
    private val TAG = AddCarFragment::class.java.simpleName
    private lateinit var root: View
    private val uiUtilInstance = UiUtils()
    private val viewModel: UserViewModel by viewModels()
    private lateinit var pb: ProgressBar
    private val ctx = ApplicationUtils.getContext()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_add_car, container, false)

        root.findViewById<MaterialButton>(R.id.mtb_fill_car).setOnClickListener {
            saveCar()
            findNavController().popBackStack()
        }
        root.findViewById<MaterialButton>(R.id.mtb_cancel_fill_car).setOnClickListener {
            findNavController().popBackStack()
        }



        return root
    }

    private fun saveCar() {
        try {


            var chargerType = "Normal"
            if (root.findViewById<RadioButton>(R.id.radio_fast).isChecked) {
                chargerType = "Fast"
            }
            if (root.findViewById<RadioButton>(R.id.radio_slow).isChecked) {
                chargerType = "Slow"
            }

            val evCar = EVCar(
                carName = root.findViewById<TextInputEditText>(R.id.etCarName).text.toString(),
                carAge = root.findViewById<TextInputEditText>(R.id.etCarAge).text.toString()
                    .toInt(),
                carMileage = root.findViewById<TextInputEditText>(R.id.etCarMileage).text.toString()
                    .toInt(),
                carBatterCapacity = root.findViewById<TextInputEditText>(R.id.etCarBattery).text.toString()
                    .toInt(),
                carConnector = root.findViewById<TextInputEditText>(R.id.etCarConnector).text.toString(),
                carChargerType = chargerType
            )

            EVCarStorage.saveCar(requireContext(), evCar)
            Toast.makeText(requireContext(), "New EV Car Added Successfully", Toast.LENGTH_SHORT)
                .show()

        } catch (e: java.lang.Exception) {
            Toast.makeText(
                requireContext(),
                "Unable to add car. Add inputs carefully",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }


}