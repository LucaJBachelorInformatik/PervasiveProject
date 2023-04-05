package hft.pervasive.tempSense

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import hft.pervasive.tempSense.databinding.ActivityMainBinding
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.temporal.Temporal
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var button: Button
    private lateinit var lineGraphView: GraphView
    private lateinit var tempAsDataPoints: Array<DataPoint?>
    private lateinit var temperatureModals: MutableList<TemperatureModal>
    private lateinit var volleyQueue: RequestQueue
    private var sizeOfArray = 10000;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lineGraphView = findViewById(R.id.idGraphView)
        button = findViewById(R.id.loadGraphButton)
        button.setOnClickListener { createGraph() }
        volleyQueue = Volley.newRequestQueue(this)
        loadGraphAndJSON()
    }
    private fun loadGraphAndJSON(){
        temperatureModals = mutableListOf()
        // ArraySize wird nacher zur Größe des JSON Arrays geändert und returned
        var arraySize = 0;

        // HTTP Request vorbereiten
        val url = "http://34.90.125.247:8080/api/v1/temperatures"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
//                Log.e("Main Activity", "${response.length()}" )
                sizeOfArray = response.length();
                for (i in 0 until response.length()){
                    val responseEntry = response[i] as JSONObject
                    val entryTemp = responseEntry.get("temperatureValue") as Double
                    val entryTimeString = responseEntry.getString("localDateTime")
                    val entryTime = LocalDateTime.parse(entryTimeString as CharSequence)
                    temperatureModals.add(TemperatureModal(entryTime, entryTemp))
                    Log.e("Data", "entryTemp : $entryTemp")
                    Log.e("Data", "entryTime = $entryTime")
                }
            },
            { error ->
                Log.e(
                    "Main Activity",
                    "JSON Load Error in jsonArrayRequest ${error.localizedMessage}"
                )
            }
        )
        volleyQueue.add(jsonArrayRequest)
    }
    private fun createGraph(){
        temperatureModals.sortBy { temperatureModal -> temperatureModal.time.dayOfYear}
        tempAsDataPoints = Array(temperatureModals.size) {DataPoint(1.0,1.0)}
        Log.e("Data", "TempAsDataPoints size: ${tempAsDataPoints.size}")
        for ((index, tempModal) in temperatureModals.withIndex()) {
            tempAsDataPoints[index]  = DataPoint(tempModal.time.dayOfYear.toDouble(),
                                                 tempModal.tempValue)
        }
        for (entry in tempAsDataPoints) {
            Log.e("Data", "TempAsDataPoints: $entry")
        }
        val series = LineGraphSeries<DataPoint>(tempAsDataPoints)
        lineGraphView.animate()
        lineGraphView.viewport.isScrollable = true
        lineGraphView.viewport.isScalable = true
        lineGraphView.viewport.setScalableY(true)
        lineGraphView.viewport.setScrollableY(true)
        series.color = R.color.purple_200
        lineGraphView.addSeries(series)
    }
    // hi
}