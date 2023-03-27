package com.example.ecoroute.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.example.ecoroute.R
import com.example.ecoroute.models.EVCar
import com.example.ecoroute.ui.user.EVCarStorage
import com.google.android.material.button.MaterialButton
import okhttp3.internal.notifyAll


@SuppressLint("SetTextI18n")
class EVCarsListAdapter(val itemClick: OnItemClickListener) :
    RecyclerView.Adapter<EVCarsListAdapter.MyViewHolder>() {


    //Initialize an empty list of the dataclass T
    var lst: MutableList<EVCar> = mutableListOf()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val evCarName = itemView.findViewById<TextView>(R.id.tv_ev_car_name)
        private val evCarCharger = itemView.findViewById<TextView>(R.id.tv_ev_car_charger)
        private val evCarConfiguration = itemView.findViewById<TextView>(R.id.tv_ev_car_config)
        private val evCarCard = itemView.findViewById<CardView>(R.id.cv_car)

        //Bind a single item
        fun bindPost(_listItem: EVCar, itemClick: OnItemClickListener) {
            with(_listItem) {


                evCarName.text = _listItem.carName
                evCarCharger.text =
                    _listItem.carConnector + " " + _listItem.carChargerType + " Charger"
                evCarConfiguration.text =
                    _listItem.carBatterCapacity.toString() + " KwH. Runs " + _listItem.carMileage.toString() + " miles. Age =" + _listItem.carAge.toString() + " days."


                itemView.setOnClickListener {
                    itemClick.clickThisItem(_listItem)
                }


            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.car_item, parent, false)
        return EVCarsListAdapter.MyViewHolder(view)

    }

    override fun getItemCount(): Int {
        return lst.size

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.bindPost(lst[position], itemClick)
        val currentCarPosition = EVCarStorage.getCurrentCarPosition(holder.itemView.context)
        if (position == currentCarPosition) {
            holder.itemView.findViewById<CardView>(R.id.cv_car)
                .setBackgroundColor(holder.itemView.resources.getColor(R.color.blue_light))
        } else {
            holder.itemView.findViewById<CardView>(R.id.cv_car)
                .setBackgroundColor(holder.itemView.resources.getColor(R.color.colorWhite))
        }

        holder.itemView.findViewById<MaterialButton>(R.id.mtb_deleter_car).setOnClickListener {
            deleteCar(holder.itemView.context, position)
        }
        holder.itemView.setOnClickListener {
            EVCarStorage.selectCar(position, holder.itemView.context)
            notifyDataSetChanged()
        }
    }

    private fun deleteCar(context: Context, position: Int) {

        // Remove car from list
        lst.removeAt(position)
        // Update saved car list in shared preferences
        EVCarStorage.editCars(context, lst)
        notifyItemRemoved(position)

    }

}

interface OnItemClickListener {
    fun clickThisItem(_listItem: EVCar)
}