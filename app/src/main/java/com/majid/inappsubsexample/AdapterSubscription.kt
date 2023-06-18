package com.majid.inappsubsexample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.majid.inappsubsexample.databinding.ItamSubscriptionBinding

class AdapterSubscription(private val list: ArrayList<ItemDs>) :
    RecyclerView.Adapter<AdapterSubscription.SubscriptionViewHolder>() {


    private lateinit var mListener: IListener

    interface IListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: IListener) {
        mListener = listener
    }

    class SubscriptionViewHolder(val binding: ItamSubscriptionBinding, listener: IListener) :
        RecyclerView.ViewHolder(binding.root) {


            init {
                binding.root.setOnClickListener {
                    listener.onItemClick(adapterPosition)
                }
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val itemBinding =
            ItamSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubscriptionViewHolder(itemBinding,mListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val currentItem = list.get(position)

        holder.binding.tvSubPlan.text = currentItem.subsName
        holder.binding.tvPlanprice.text = currentItem.formattedPrice
    }
}