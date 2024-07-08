package com.example.auctionkingdom

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LibraryActivity : AppCompatActivity() {

    private lateinit var fullScreenImageView: ImageView
    private var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        // 뒤로 가기 버튼 설정
        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        // 전체 화면 이미지 뷰 설정
        fullScreenImageView = findViewById(R.id.full_screen_image_view)
        fullScreenImageView.setOnClickListener {
            toggleFullScreenImage(null)
        }

        // RecyclerView 설정
        val recyclerView: RecyclerView = findViewById(R.id.card_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2열의 그리드 레이아웃
        recyclerView.adapter = LibraryAdapter(getCardData(), this::toggleFullScreenImage)
    }

    private fun getCardData(): List<Int> {
        // 여기서 카드에 사용할 이미지 리소스 ID 리스트를 반환합니다.
        return listOf(
            R.drawable.card_image_1,
            R.drawable.card_image_2,
            R.drawable.card_image_3,
            R.drawable.card_image_4,
            R.drawable.card_image_5,
            R.drawable.card_image_6,
            R.drawable.card_image_7,
            R.drawable.card_image_8,
            R.drawable.card_image_9,
            R.drawable.card_image_10
        )
    }

    private fun toggleFullScreenImage(imageResId: Int?) {
        if (isFullScreen) {
            fullScreenImageView.visibility = View.GONE
            isFullScreen = false
        } else {
            if (imageResId != null) {
                fullScreenImageView.setImageResource(imageResId)
                fullScreenImageView.visibility = View.VISIBLE
                isFullScreen = true
            }
        }
    }
}
