package com.example.ecoroute.ui.user

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ecoroute.R
import com.example.ecoroute.models.EVCar
import com.example.ecoroute.ui.home.HomeFragment
import com.example.ecoroute.utils.ApplicationUtils
import com.example.ecoroute.utils.ConnectorType
import com.example.ecoroute.utils.UiUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText


class AddCarFragment : Fragment() {
    private val TAG = AddCarFragment::class.java.simpleName
    private lateinit var root: View
    private val uiUtilInstance = UiUtils()
    private val viewModel: UserViewModel by viewModels()
    private lateinit var pb: ProgressBar
    private val ctx = ApplicationUtils.getContext()
    private lateinit var connector_list: MutableList<String>
    private lateinit var checked_list: MutableMap<String, Boolean>
    private lateinit var selected_list: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_add_car, container, false)

        connector_list = ConnectorType.extarct_suitable_connector()
        checked_list = mutableMapOf()
        selected_list = mutableListOf()

        clearCheckList()
        root.findViewById<TextView>(R.id.tilCarConnector).setOnClickListener {
            buildConnectorSelectorDialog()
        }
        root.findViewById<MaterialButton>(R.id.mtb_fill_car).setOnClickListener {
            saveCar()
            findNavController().popBackStack()
        }
        root.findViewById<MaterialButton>(R.id.mtb_cancel_fill_car).setOnClickListener {
            findNavController().popBackStack()
        }



        return root
    }

    private fun clearCheckList() {
        for (connector in connector_list) {
            checked_list[connector] = false
        }
    }


    private fun buildConnectorSelectorDialog() {
        val n = connector_list.size
        var alreadyCheckedItems = BooleanArray(n)
        for (i in 0 until n) {
            alreadyCheckedItems[i] = checked_list[connector_list[i]]!!
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Options")
        builder.setMultiChoiceItems(connector_list.toTypedArray(), alreadyCheckedItems) { _di, which, isChecked ->
            checked_list[connector_list[which]] = isChecked // Update the mutable map
        }
        builder.setPositiveButton("OK") { _, _ ->
            selected_list.clear()
            for (check in checked_list) {
                if (check.value) {
                    selected_list.add(check.key)
                }
            }
            root.findViewById<TextView>(R.id.tvConnectorList).text = formatConnectors()
        }
        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()


    }

    private fun formatConnectors(): String {
        var ls = ""
        for (connector in selected_list) {
            ls += "($connector) "
        }


        return ls
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
                carConnector = selected_list,
                carChargerType = chargerType
            )

            EVCarStorage.saveCar(requireContext(), evCar)
            Toast.makeText(requireContext(), "New EV Car Added Successfully", Toast.LENGTH_SHORT)
                .show()

        } catch (e: java.lang.Exception) {
            Toast.makeText(
                requireContext(), "Unable to add car. Add inputs carefully", Toast.LENGTH_SHORT
            ).show()
        }
    }


}