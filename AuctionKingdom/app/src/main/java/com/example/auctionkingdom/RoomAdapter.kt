package com.example.auctionkingdom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RoomAdapter(private val rooms: List<Room>) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_item, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.bind(room)
    }

    override fun getItemCount(): Int = rooms.size

    class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val roomCodeTextView: TextView = view.findViewById(R.id.room_code_text_view)
        private val playerCountTextView: TextView = view.findViewById(R.id.player_count_text_view)

        fun bind(room: Room) {
            roomCodeTextView.text = "Room: ${room.code}"
            playerCountTextView.text = "Players: ${room.playerCount}/2"
        }
    }
}
