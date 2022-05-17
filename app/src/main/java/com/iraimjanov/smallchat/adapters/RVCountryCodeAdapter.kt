package com.iraimjanov.smallchat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.iraimjanov.smallchat.cc.CountryDB
import com.iraimjanov.smallchat.data.PublicData
import com.iraimjanov.smallchat.databinding.FragmentWelcomeBinding
import com.iraimjanov.smallchat.databinding.ItemCountryCodeBinding

class RVCountryCodeAdapter(
    private val listCountry: List<CountryDB>,
    private val navController: NavController,
) :
    RecyclerView.Adapter<RVCountryCodeAdapter.VH>() {

    private lateinit var welcomeBinding: FragmentWelcomeBinding
    inner class VH(var itemRV: ItemCountryCodeBinding) : RecyclerView.ViewHolder(itemRV.root) {
        fun onBind(country: CountryDB) {
            itemRV.code.text = country.codes.toString()
            itemRV.name.text = country.names.toString()
            itemRV.root.setOnClickListener {
                PublicData.countryDB = country
                navController.popBackStack()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCountryCodeBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(listCountry[position])

    }

    override fun getItemCount(): Int = listCountry.size

}