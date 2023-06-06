package hft.pervasive.tempSense

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import hft.pervasive.tempSense.databinding.ActivityMainBinding
import org.json.JSONObject
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var buttonTest: Button
    private lateinit var buttonDaily: Button
    private lateinit var buttonGraph: Button
    private lateinit var lineGraphView: GraphView
    private lateinit var tempAsDataPoints: Array<DataPoint?>
    private lateinit var temperatureModals: MutableList<TemperatureModal>
    private lateinit var volleyQueue: RequestQueue

    // Variable um Graphen die ganze (total) Zeit anzeigen zu lassen oder die täglichen Werte
    private var sizeOfArray = 10000;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lineGraphView = findViewById(R.id.idGraphView)

        buttonTest = findViewById(R.id.buttonTest)
        buttonTest.setOnClickListener({loadTest()})

        buttonDaily = findViewById(R.id.buttonDaily)
        buttonDaily.setOnClickListener{loadDaily()}

        buttonGraph = findViewById(R.id.buttonGraph)
        buttonGraph.setOnClickListener { createGraph() }

        volleyQueue = Volley.newRequestQueue(this)
    }

    private fun loadTotal(){
        temperatureModals = mutableListOf()
        // HTTP Request vorbereiten
        val url = "http://34.90.125.247:8080/api/v1/temperatures"
        createJsonRequest(url);
    }
    private fun loadDaily(){
        temperatureModals = mutableListOf()
        lineGraphView.removeAllSeries()
        // HTTP Request vorbereiten
        val url = "http://34.90.125.247:8080/api/v1/temperatures/test/daily"
        createJsonRequest(url);
        Log.e("Data", "Temperature modal size: ${temperatureModals.size}")
    }
    private fun loadTest(){
        postReqForTestData()
        var url = "http://34.90.125.247:8080/api/v1/temperatures/test/daily"
        createJsonRequest(url)
    }
    private fun postReqForTestData(){
        val url = "http://34.90.125.247:8080/api/v1/temperatures/test/config"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            null,
            null,
            null
        )
        volleyQueue.add(jsonObjectRequest)
        Log.e("Data", "Temperature modal size: ${temperatureModals.size}")
    }
    private fun createJsonRequest(url: String) {
        temperatureModals = mutableListOf()
        lineGraphView.removeAllSeries()
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
        tempAsDataPoints = Array(temperatureModals.size) {DataPoint(1.0,1.0)}
        Log.e("Data", "TempAsDataPoints size: ${tempAsDataPoints.size}")
        temperatureModals.sortBy{ temperatureModal -> temperatureModal.time.hour}
        for((index, tempModal) in temperatureModals.withIndex()) {
            tempAsDataPoints[index] = DataPoint(tempModal.time.hour.toDouble(), tempModal.tempValue)
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
        lineGraphView.viewport.scrollToEnd()
    }
}