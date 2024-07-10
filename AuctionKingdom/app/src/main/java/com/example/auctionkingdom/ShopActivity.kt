package com.example.auctionkingdom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ShopActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var coinTextView: TextView
    private lateinit var itemListRecyclerView: RecyclerView
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        email = intent.getStringExtra("email") ?: ""
        coinTextView = findViewById(R.id.coin_text)
        itemListRecyclerView = findViewById(R.id.item_list_recycler_view)
        val backButton: ImageView = findViewById(R.id.back_button)

        backButton.setOnClickListener {
            finish()
        }

        fetchUserData()
        setupItemList()
    }

    private fun fetchUserData() {
        val request = Request.Builder()
            .url("http://172.10.7.80:80/api/getUser?email=$email")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    coinTextView.text = "Failed to load data"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val coins = jsonResponse.getInt("coins")
                        coinTextView.text = coins.toString()
                    } catch (e: Exception) {
                        coinTextView.text = "Failed to parse data"
                    }
                }
            }
        })
    }

    private fun setupItemList() {
        val items = listOf(
            Item("item1", "몰보냥", 100, "상대의 배팅 금액을 최초 1회 확인한다.", R.drawable.item_icon_1),
            Item("item2", "부자냥", 200, "시작할 때 200코인 추가 제공받는다.", R.drawable.item_icon_2),
            Item("item3", "냥평성대", 300, "승리 시 남은 돈의 1.2배를 획득한다.", R.drawable.item_icon_3)
        )

        itemListRecyclerView.layoutManager = LinearLayoutManager(this)
        itemListRecyclerView.adapter = ItemAdapter(items, email, coinTextView, client)
    }
}

data class Item(val id: String, val name: String, val cost: Int, val description: String, val imageResId: Int)

class ItemAdapter(
    private val items: List<Item>,
    private val email: String,
    private val coinTextView: TextView,
    private val client: OkHttpClient
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, email, coinTextView, client)
    }

    override fun getItemCount() = items.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.item_name)
        private val itemDescriptionTextView: TextView = itemView.findViewById(R.id.item_description)
        private val itemCostTextView: TextView = itemView.findViewById(R.id.item_cost)
        private val itemImageView: ImageView = itemView.findViewById(R.id.item_image)
        private val buyButton: Button = itemView.findViewById(R.id.buy_button)
        private val itemOwnedTextView: TextView = itemView.findViewById(R.id.item_owned)

        fun bind(item: Item, email: String, coinTextView: TextView, client: OkHttpClient) {
            itemNameTextView.text = item.name
            itemDescriptionTextView.text = item.description
            itemCostTextView.text = item.cost.toString()
            itemImageView.setImageResource(item.imageResId)

            fetchItemOwned(email, item.id, itemOwnedTextView, client)

            buyButton.setOnClickListener {
                val json = JSONObject()
                json.put("email", email)
                json.put("item", item.id)
                json.put("cost", item.cost)

                val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("http://172.10.7.80:80/api/updateUserItems")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // 실패 처리
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string()
                            val jsonResponse = JSONObject(responseData)
                            val newCoins = jsonResponse.getInt("coins")

                            (itemView.context as AppCompatActivity).runOnUiThread {
                                coinTextView.text = newCoins.toString()
                                fetchItemOwned(email, item.id, itemOwnedTextView, client)
                            }
                        }
                    }
                })
            }
        }

        private fun fetchItemOwned(email: String, itemId: String, itemOwnedTextView: TextView, client: OkHttpClient) {
            val request = Request.Builder()
                .url("http://172.10.7.80:80/api/getUser?email=$email")
                .get()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // 실패 처리
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    (itemView.context as AppCompatActivity).runOnUiThread {
                        try {
                            val jsonResponse = JSONObject(responseData)
                            val itemOwned = jsonResponse.getInt(itemId)
                            itemOwnedTextView.text = "Owned: $itemOwned"
                        } catch (e: Exception) {
                            itemOwnedTextView.text = "Owned: 0"
                        }
                    }
                }
            })
        }
    }
}
