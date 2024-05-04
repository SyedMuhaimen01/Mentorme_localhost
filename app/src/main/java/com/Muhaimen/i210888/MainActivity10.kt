package com.Muhaimen.i210888
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
class Mentor (
    val id: String,
    val name: String,
    val title: String,
    val description: String,
    val imagePath: String,
    val sessionPrice: Double,
    val availability: String,
    val rating: Double
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        0.0,
        "",
        0.0
    )
}


class MainActivity10 : AppCompatActivity() {

    private lateinit var resultMentorList: ArrayList<Mentor>
    private lateinit var resultMentorAdapter: searchAdapter
    private val ip = "192.168.100.8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main10)

        setupSpinner()
        setupRecyclerView()
        setupButtonListeners()

        // Initially fetch all mentors
        retrieveAndSetMentorData("all")
    }

    private fun setupSpinner() {
        val items = arrayOf("All", "Filter1", "Filter2") // Add your filter options here
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner: Spinner = findViewById(R.id.spinner3)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = items[position]
                retrieveAndSetMentorData(selectedFilter)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        resultMentorList = ArrayList()
        resultMentorAdapter = searchAdapter(resultMentorList, this)
        val recyclerView: RecyclerView = findViewById(R.id.searchResult)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = resultMentorAdapter
    }

    private fun setupButtonListeners() {
        // Setup button listeners as per your requirements
    }

    private fun retrieveAndSetMentorData(filter: String) {
        val url = "http://$ip/get_mentors.php?filter=$filter"

        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                processMentorData(response)
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Error retrieving mentor data: ${error.message}", error)
                Toast.makeText(this, "Error retrieving mentor data: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun processMentorData(response: String) {
        try {
            val mentorList = ArrayList<Mentor>()
            val mentorsArray = JSONArray(response)
            for (i in 0 until mentorsArray.length()) {
                val mentorObject = mentorsArray.getJSONObject(i)
                val mentor = Mentor(
                    mentorObject.getString("id"),
                    mentorObject.getString("name"),
                    mentorObject.getString("title"),
                    mentorObject.getString("description"),
                    mentorObject.getString("imagePath"),
                    mentorObject.getDouble("sessionPrice"),
                    mentorObject.getString("availability"),
                    mentorObject.getDouble("rating")
                )
                mentorList.add(mentor)
            }
            resultMentorList.clear()
            resultMentorList.addAll(mentorList)
            resultMentorAdapter.notifyDataSetChanged()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


}
