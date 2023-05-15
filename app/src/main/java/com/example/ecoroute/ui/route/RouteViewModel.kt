package com.example.ecoroute.ui.route

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ecoroute.interfaces.RetrofitClient
import com.example.ecoroute.models.responses.EcorouteAPIResponse
import com.example.ecoroute.models.responses.EcorouteResponse
import com.example.ecoroute.utils.UiUtils
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Response

@SuppressLint("LogNotTimber", "StringFormatInvalid", "SetTextI18n")
@Keep
class RouteViewModel : ViewModel() {


    val TAG = RouteViewModel::class.java.simpleName
    private val ecorouteService = RetrofitClient.ecorouteInterface()

    var successful: MutableLiveData<Boolean> = MutableLiveData()
    var message: MutableLiveData<String> = MutableLiveData()
    var in_progress : MutableLiveData<Boolean> = MutableLiveData()

    private var mResponse: MutableLiveData<ArrayList<EcorouteAPIResponse.EcorouteAPIResponseItem>> =
        MutableLiveData()

    fun getOptimalPath(url: String, pb: ProgressBar, csl: ConstraintLayout): MutableLiveData<ArrayList<EcorouteAPIResponse.EcorouteAPIResponseItem>> {

        mResponse = optimalPath(url,pb,csl)
        return mResponse
    }


    private fun optimalPath(url: String, pb: ProgressBar, csl: ConstraintLayout): MutableLiveData<ArrayList<EcorouteAPIResponse.EcorouteAPIResponseItem>> {


        Log.e(TAG, "PathURL:  $url")

        ecorouteService.getEcoroutePath(url)
            .enqueue(object :
                retrofit2.Callback<ArrayList<EcorouteAPIResponse.EcorouteAPIResponseItem>> {
                override fun onFailure(
                    call: Call<ArrayList<EcorouteAPIResponse.EcorouteAPIResponseItem>>,
                    t: Throwable
                ) {
                    successful.value = false
                    in_progress.value = false
                    message.value = UiUtils().returnStateMessageForThrowable(t)
                    UiUtils().logThrowables(TAG, t)
                    pb.visibility = View.INVISIBLE
                    Snackbar.make(csl, "Unable to find path", Snackbar.LENGTH_SHORT).show()

                }

                override fun onResponse(
                    call: Call<ArrayList<EcorouteAPIResponse.EcorouteAPIResponseItem>>,
                    response: Response<ArrayList<EcorouteAPIResponse.EcorouteAPIResponseItem>>
                ) {

                    in_progress.value = false
                    Log.e(TAG, "Response: $response")
                    if (response.isSuccessful && response.body() != null) {

                        successful.value = true
                        message.value = "Loaded path"
                        mResponse.value = response.body()


                    } else {

                        successful.value = false
                        message.value = "Unable to find path"
                    }


                }
            })


        return mResponse


    }
}