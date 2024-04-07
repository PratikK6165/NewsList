package com.example.newslist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {


    private lateinit var newsList: MutableList<News>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        newsList = mutableListOf()
        adapter = NewsAdapter(newsList)
        recyclerView.adapter = adapter

        val btnOldToNew = findViewById<Button>(R.id.btnOldToNew)
        val btnNewToOld = findViewById<Button>(R.id.btnNewToOld)

        fetchData()


        btnOldToNew.setOnClickListener {
            sortAndDisplayArticles(oldToNew = true)
        }

        btnNewToOld.setOnClickListener {
            sortAndDisplayArticles(oldToNew = false)
        }

    }

    private fun sortAndDisplayArticles(oldToNew: Boolean) {
        newsList.sortBy { it.title } // Sort based on the title
        if (!oldToNew) {
            newsList.reverse()
        }
        adapter.notifyDataSetChanged()
    }


    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url =
                    URL("https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json")
                val urlConnection = url.openConnection() as HttpURLConnection
                val inputStream = urlConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))

                val response = StringBuilder()
                var inputLine: String?

                while (bufferedReader.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }

                bufferedReader.close()

                val jsonResponse = JSONObject(response.toString())
                val articles = jsonResponse.getJSONArray("articles")

                for (i in 0 until articles.length()) {
                    val article = articles.getJSONObject(i)
                    val headline = article.getString("title")
                    val url = article.getString("url")
                    newsList.add(News(headline, url)) // Create News object instead of Pair
                }

                runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


//    private fun parseJson(json: String) {
//        val jsonObject = JSONObject(json)
//        val articles = jsonObject.getJSONArray("articles")
//        for (i in 0 until articles.length()) {
//            val article = articles.getJSONObject(i)
//            val title = article.getString("title")
//            val url = article.getString("url")
//            newsList.add(News(title, url))
//        }
//        runOnUiThread {
//            recyclerView.adapter = NewsAdapter(newsList)
//        }
//    }

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