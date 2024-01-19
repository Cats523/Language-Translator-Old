package com.wonderapps.translator.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wonderapps.translator.R
import com.wonderapps.translator.databinding.FragmentOCRBinding
import com.wonderapps.translator.databinding.SampleLanguageOcrItemBinding
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage


import kotlin.properties.Delegates

class OCRLanguageAdapter(var context : Context, var ocrLanguageList: ArrayList<ConversationLanguage>, var index : Int, var binding : FragmentOCRBinding, var dialog : Dialog ) : RecyclerView.Adapter<OCRLanguageAdapter.ViewHolder>() {






    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(SampleLanguageOcrItemBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.languageFlag.setImageResource(ocrLanguageList[position].languageFlag)
        holder.binding.languageName.text=ocrLanguageList[position].languageName

        if (ocrLanguageList[position].languageName=="English"){
            holder.binding.downloadIconInOCR.setImageResource(R.drawable.ic_baseline_download_done_24)
        }

        holder.binding.downloadIconInOCR.setOnClickListener {
            if(isWiFiConnected()){
                val dialog = ProgressDialog(context)
                dialog.setTitle(context.getString(R.string.Downloading))
                dialog.setMessage(context.getString(R.string.DownloadDialogMsg))
                dialog.setCancelable(false)
                dialog.create()
                dialog.show()
            }
            else{
                Toast.makeText(context,"Please connect to WIFI",Toast.LENGTH_LONG).show()
            }
        }


        holder.itemView.setOnClickListener{

            if (index==1){
                val firstCardLanguage=ocrLanguageList[position].languageName
                val firstLanguageModelCode=ocrLanguageList[position].languageCode
                val  firstLanguageCode=ocrLanguageList[position].languageCode

               val  sharedPreferences1=context.getSharedPreferences("OCR_LANGUAGE_PREFERENCE",Context.MODE_PRIVATE)
              val   editor1=sharedPreferences1.edit()
                editor1.putString("OCR_FIRST_LANGUAGE_NAME",firstCardLanguage)
                editor1.putString("OCR_FIRST_LANGUAGE_CODE",firstLanguageCode.toString())
                editor1.putString("OCR_FIRST_LANGUAGE_MODEL_CODE",firstLanguageModelCode)
                editor1.putInt("OCR_FIRST_LANGUAGE_POSITION",position)
                editor1.apply()

                dialog.dismiss()
            }
            if (index==2){

            val     secondCardLanguage=ocrLanguageList[position].languageName
               val  secondLanguageCode=ocrLanguageList[position].languageCode
              val   secondLanguageModelCode=ocrLanguageList[position].languageVoiceCode

               val  sharedPreferences1=context.getSharedPreferences("OCR_LANGUAGE_PREFERENCE",Context.MODE_PRIVATE)
             val   editor1=sharedPreferences1.edit()
                editor1.putString("OCR_SECOND_LANGUAGE_NAME",secondCardLanguage)
                editor1.putString("OCR_SECOND_LANGUAGE_CODE",secondLanguageCode.toString())
                editor1.putString("OCR_SECOND_LANGUAGE_MODEL_CODE",secondLanguageModelCode)
                editor1.putInt("OCR_SECOND_LANGUAGE_POSITION",position)
                editor1.apply()


                dialog.dismiss()
            }
            dialog.cancel()


        }
    }

    @SuppressLint("NewApi")
    fun isWiFiConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.UseWifiMsg),
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.UseWifiMsg),
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
            }
        }
        return false
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return ocrLanguageList.size
    }

    class ViewHolder(val binding: SampleLanguageOcrItemBinding) : RecyclerView.ViewHolder(binding.root)


}