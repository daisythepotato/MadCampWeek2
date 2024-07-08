package com.example.auctionkingdom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class RankingItem(val profileImage: Int, val nickname: String, val kingdomName: String, val score: Int)

class RankingAdapter(private val rankingData: List<RankingItem>) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ranking_item, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val item = rankingData[position]

        // 프로필 이미지 로드 (숫자 값을 실제 리소스 ID로 변환)
        val profileImageRes = item.profileImage
        holder.profileImage.setImageResource(profileImageRes)

        holder.rankText.text = (position + 1).toString() // 순위 표시
        holder.nameText.text = item.nickname
        holder.kingdomNameText.text = item.kingdomName
        holder.scoreText.text = item.score.toString()

        // 티어 이미지 설정
        val tierImageRes = when {
            item.score < 1000 -> R.drawable.bronze_flag
            item.score < 2000 -> R.drawable.silver_flag
            else -> R.drawable.gold_flag
        }
        holder.tierImage.setImageResource(tierImageRes)
    }

    override fun getItemCount(): Int {
        return rankingData.size
    }



    class RankingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.rank_text) // 순위 텍스트뷰
        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        val nameText: TextView = view.findViewById(R.id.name_text)
        val kingdomNameText: TextView = view.findViewById(R.id.kingdom_name_text)
        val scoreText: TextView = view.findViewById(R.id.score_text)
        val tierImage: ImageView = view.findViewById(R.id.tier_image)
    }
}
