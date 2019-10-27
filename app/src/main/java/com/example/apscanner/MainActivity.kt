package com.example.apscanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toolbar
import kotlin.math.round

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.pow
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Handler
import android.os.Looper
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.log10


class MainActivity : AppCompatActivity() {
    private var mExampleList: ArrayList<ExampleItem>? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    private val images = arrayOf(R.drawable.ic_wifi_0, R.drawable.ic_wifi_1, R.drawable.ic_wifi_2, R.drawable.ic_wifi_3, R.drawable.ic_wifi_4)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        createApList()
        buildRecyclerView()

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                refreshApList()
                mainHandler.postDelayed(this, 4000)
            }
        })

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        refreshApList()
        return super.onOptionsItemSelected(item)
    }


    fun refreshApList() {
        mExampleList!!.clear()

        var newList: ArrayList<ExampleItem>? = ArrayList()
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    println("Success!! Printing new results")
                } else {
                    println("Scan Failed!! Printing provious values...")
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        applicationContext.registerReceiver(wifiScanReceiver, intentFilter)

        @Suppress("DEPRECATION")
        val success = wifiManager.startScan()

        if(!success){
            println("Scan Failed!")
        }

        val wifiList = wifiManager.scanResults

        for (scanResult in wifiList) {
            val exp = (27.55 - 20 * log10(scanResult.frequency.toDouble()) + abs(scanResult.level)) / 20.0
            val distanceM = (10.0).pow(exp)
            val i = WifiManager.calculateSignalLevel(scanResult.level, 5)
            val name = "".plus(scanResult.SSID).plus(" [").plus(round(distanceM * 100)/100).plus("m]")
            val details = "".plus(scanResult.level).plus("dBm - BSSID: ").plus(scanResult.BSSID).plus(" frequency: ").plus(scanResult.frequency.toDouble())
            newList!!.add(ExampleItem(images[i], name, details))
            println(name)
        }
        mExampleList!!.addAll(newList!!)
        mAdapter!!.notifyDataSetChanged()

    }


    fun createApList() {
        mExampleList = ArrayList()
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiList = wifiManager.scanResults

        for (scanResult in wifiList) {
            val exp = (27.55 - 20 * log10(scanResult.frequency.toDouble()) + abs(scanResult.level)) / 20.0
            val distanceM = (10.0).pow(exp)
            val i = WifiManager.calculateSignalLevel(scanResult.level, 5)
            val name = "".plus(scanResult.SSID).plus(" [").plus(round(distanceM * 100)/100).plus("m]")
            val details = "".plus(scanResult.level).plus("dBm - BSSID: ").plus(scanResult.BSSID)
//            val name = "".plus(scanResult.SSID).plus(" (").plus(scanResult.BSSID).plus(")")
//            val dist = "".plus(scanResult.level).plus("db, at a distance of ").plus(round(distanceM * 100)/100).plus("m")
            mExampleList!!.add(ExampleItem(images[i], name, details))
        }
    }


    fun createDemoApList() {
        mExampleList = ArrayList()

        val ssid = arrayOf("WIFI Name 1", "WIFI Name 2 5GHz", "WIFI Name 3", "WIFI Name 4", "WIFI Name 5", "WIFI Name 6", "WIFI Name 7", "WIFI Name 8 5GHz", "WIFI Name 9", "WIFI Name 10")
        val bssid = arrayOf("d5:04:46:74:c5:7e", "12:9e:33:c9:63:9a", "63:83:59:45:02:53", "8d:9a:fb:39:60:5e", "5e:f8:e4:01:2d:ca", "ca:e8:b6:88:9e:96", "1f:b4:fe:88:23:89", "12:9e:33:c9:63:9a", "63:83:59:45:02:53", "8d:9a:fb:39:60:5e")
        val frequency = arrayOf(2437, 5745, 2462, 2427, 2452, 2412, 2442, 5745, 2462, 2427)
        val level = arrayOf(-57, -64, -68, -76, -78, -81, -84, -86, -91, -94)

        for (i in 0..9) {
            val exp = (27.55 - 20 * log10(frequency[i].toDouble()) + abs(level[i])) / 20.0
            val distanceM = (10.0).pow(exp)
            val i1 = WifiManager.calculateSignalLevel(level[i], 5)
            val name = "".plus(ssid[i]).plus(" [").plus(round(distanceM * 100) / 100).plus("m]")
            val details = "".plus(level[i]).plus("dBm - BSSID: ").plus(bssid[i])
            mExampleList!!.add(ExampleItem(images[i1], name, details))
        }

    }


    fun buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView!!.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        mAdapter = ExampleAdapter(mExampleList)

        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.adapter = mAdapter
    }
}

