package com.example.ecoroute.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import com.example.ecoroute.R
import com.google.android.material.snackbar.Snackbar
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException


class UiUtils {

    fun setupGenericAdapters(items: List<String>, actv: AutoCompleteTextView, ctx: Context) {

        val adapter = ArrayAdapter(ctx, R.layout.ev_list_item, items)
        (actv as? AutoCompleteTextView)?.setAdapter(adapter)
    }


    fun setupLocationAdapters(actv: AutoCompleteTextView, ctx: Context): ArrayAdapter<String> {

        val adapter = ArrayAdapter<String>(ctx, android.R.layout.simple_dropdown_item_1line)
        (actv as? AutoCompleteTextView)?.setAdapter(adapter)
        return adapter
    }

    fun showIndefiniteSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).show()
    }

    fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showProgress(view: View, pb: ProgressBar, ctx: Context) {
        pb.visibility = View.VISIBLE
    }

    fun hideProgress(view: View, pb: ProgressBar, ctx: Context) {
        pb.visibility = View.GONE
    }

    fun logThrowables(TAG: String, t: Throwable) {
        Log.e(TAG, "Throwable $t with cause = " + t.cause.toString())
    }

    fun logExceptions(TAG: String, e: Exception) {
        Log.e(
            TAG,
            "Exception $e with cause = " + e.cause.toString() + " and message = " + e.message.toString()
        )
    }

    fun returnStateMessageForThrowable(throwable: Throwable): String {
        var message: String = ""


        when (throwable) {
            is IOException -> {
                message = ApplicationUtils.getContext()
                    .getString(R.string.error_please_check_internet)
            }
            is TimeoutException -> {
                message = ApplicationUtils.getContext()
                    .getString(R.string.error_request_timed_out)
            }

            //##Str for it
            is HttpException -> {
                val httpException = throwable
                val response = httpException.response()?.errorBody()?.string()

                message = response!!
            }
            else -> {
                message = ApplicationUtils.getContext()
                    .getString(R.string.error_something_went_wrong)
            }
        }

        return message
    }

}