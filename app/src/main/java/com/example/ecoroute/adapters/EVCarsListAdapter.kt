package com.example.ecoroute.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoroute.R
import com.example.ecoroute.models.EVCar

class EVCarsListAdapter(val itemClick: OnItemClickListener) :
    RecyclerView.Adapter<EVCarsListAdapter.MyViewHolder>() {


    //Initialize an empty list of the dataclass T
    var lst: List<EVCar> = mutableListOf()
    var currentCarPosition = 0

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val evCarName = itemView.findViewById<TextView>(R.id.tv_ev_car_name)
        private val evCarDescription = itemView.findViewById<TextView>(R.id.tv_ev_car_des)
        private val evCarImage = itemView.findViewById<ImageView>(R.id.iv_ev_car_image)
        private val evCarCard = itemView.findViewById<CardView>(R.id.cv_car)

        //Bind a single item
        fun bindPost(_listItem: EVCar, itemClick: OnItemClickListener) {
            with(_listItem) {


                evCarName.text = _listItem.carName
                evCarDescription.text = itemView.context.getString(
                    R.string.recharges,
                    _listItem.carType.chargingSpeed.toString(),
                    _listItem.carType.plugType
                )


                val resID: Int = itemView.context.resources.getIdentifier(
                    _listItem.carType.carImageUrl,
                    "drawable",
                    itemView.context.packageName
                )

                Glide.with(itemView.context)
                    .load(
                        itemView.context.resources.getDrawable(resID)
                    )
                    .placeholder(R.drawable.ic_baseline_electric_car_24)
                    .error(R.drawable.ic_baseline_electric_car_24).into(evCarImage)

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

        if (position == currentCarPosition) {
            holder.itemView.findViewById<CardView>(R.id.cv_car)
                .setBackgroundColor(holder.itemView.resources.getColor(R.color.colorPrimary))
        } else {
            holder.itemView.findViewById<CardView>(R.id.cv_car)
                .setBackgroundColor(holder.itemView.resources.getColor(R.color.colorWhite))
        }


        holder.itemView.setOnClickListener {
            currentCarPosition = position
            notifyDataSetChanged()
        }
    }

}

interface OnItemClickListener {
    fun clickThisItem(_listItem: EVCar)
}