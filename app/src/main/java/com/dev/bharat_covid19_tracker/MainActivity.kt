package com.dev.bharat_covid19_tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var stateAdaptor: StateAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list.addHeaderView(LayoutInflater.from(this).inflate(R.layout.item_header,list,false))
        fetchresults()
        refreshApp()

    }




    private fun fetchresults() {
        GlobalScope.launch {
            val response = withContext(Dispatchers.IO) { Client.api.execute() }
            if(response.isSuccessful){
                val data = Gson().fromJson(response.body?.string(),Response::class.java)
                launch(Dispatchers.Main){
                    bindCombinedData(data.statewise[0])
                    bindStateWiseData(data.statewise.subList(1,data.statewise.size))
                }
            }
        }
    }

    private fun bindStateWiseData(subList: List<StatewiseItem>) {

        stateAdaptor = StateAdaptor(subList)
        list.adapter=stateAdaptor
    }

    private fun bindCombinedData(data: StatewiseItem) {
        val lastUpdatedTime = data.lastupdatedtime
        val simpleDateFormat = SimpleDateFormat("dd/MM/yy HH:mm:ss")
        lastupdateTv.text="Last Updated \n ${getTimeAgo(simpleDateFormat.parse(lastUpdatedTime))}"

        confirmedTv.text = data.confirmed
        activeTv.text = data.active
        recoveredTv.text = data.recovered
        deathTv.text = data.deaths


    }
    fun getTimeAgo(past: Date):String{
        val now = Date()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time-past.time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time-past.time)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time-past.time)

        return when{
            seconds<60 ->{
                "Few seconds Ago"
            }
            minutes<60 ->{
                "$minutes minutes ago"
            }
            hours <24 ->{
                "$hours hour ${minutes %60} min ago"
            }
            else ->{
                SimpleDateFormat("dd/MM/yy, hh:mm a").format(past).toString()
            }

        }
    }
    private fun refreshApp(){
        swipetofrefresh.setOnRefreshListener {
            Toast.makeText(this,"Page Refreshed !",Toast.LENGTH_SHORT).show()
            swipetofrefresh.isRefreshing=false
        }
    }

}
