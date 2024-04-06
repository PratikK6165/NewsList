package com.example.newslist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {


    private val newsList = ArrayList<News>()
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        fetchData()
    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url =
                    URL("https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                val inputStream = connection.inputStream
                val json = inputStream.bufferedReader().use { it.readText() }
                parseJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error fetching data", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun parseJson(json: String) {
        val jsonObject = JSONObject(json)
        val articles = jsonObject.getJSONArray("articles")
        for (i in 0 until articles.length()) {
            val article = articles.getJSONObject(i)
            val title = article.getString("title")
            val url = article.getString("url")
            newsList.add(News(title, url))
        }
        runOnUiThread {
            recyclerView.adapter = NewsAdapter(newsList)
        }
    }

    inner class NewsAdapter(private val newsList: List<News>) :
        RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false)
            return NewsViewHolder(view)
        }

        override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
            val news = newsList[position]
            holder.bind(news)
        }

        override fun getItemCount(): Int {
            return newsList.size
        }

        inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            fun bind(news: News) {
                titleTextView.text = news.title
                itemView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.url))
                    startActivity(intent)
                }
            }
        }
    }

    data class News(val title: String, val url: String)
}