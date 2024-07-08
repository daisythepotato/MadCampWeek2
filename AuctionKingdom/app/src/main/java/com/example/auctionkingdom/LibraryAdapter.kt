package com.example.auctionkingdom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class LibraryAdapter(
    private val cardData: List<Int>,
    private val onCardClick: (Int) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(view, onCardClick)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.cardImage.setImageResource(cardData[position])
        holder.cardImage.setOnClickListener {
            onCardClick(cardData[position])
        }
    }

    override fun getItemCount(): Int {
        return cardData.size
    }

    class CardViewHolder(view: View, onCardClick: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        val cardImage: ImageView = view.findViewById(R.id.card_image)
    }
}
