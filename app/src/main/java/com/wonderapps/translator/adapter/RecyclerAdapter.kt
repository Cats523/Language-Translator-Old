package com.wonderapps.translator.adapter


import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.wonderapps.translator.databinding.CustomLanguageDialogBinding
import com.wonderapps.translator.databinding.FragmentHomeBinding
import com.wonderapps.translator.model.SharedViewModel
import com.wonderapps.translator.utils.dataclasses.ConversationLanguage

data class RecyclerAdapter(
    private val sharedViewModel: SharedViewModel,
    private val context: Context,
    private var languageList: ArrayList<ConversationLanguage>,
    private var pos: Int,

    var binding: FragmentHomeBinding,
    var dialog: Dialog,
    var searchView: EditText
) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


    private lateinit var sharedPreferencesForDownloadIcon: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CustomLanguageDialogBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        sharedPreferencesForDownloadIcon = context.getSharedPreferences(
            "Shared_Preference_For_Download_Icon", Context.MODE_PRIVATE
        )
        editor = sharedPreferencesForDownloadIcon.edit()
        holder.binding.languageName.text = languageList[position].languageName
        val sharedPreferences = context.getSharedPreferences(
            "Translate_Language_Preference", Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        holder.binding.languageFlag.setImageResource(languageList[position].languageFlag)
        holder.itemView.setOnClickListener {
            if (pos == 1) {
                binding.language1TextViewInHomeFragment.text = languageList[position].languageName
                editor.putString("Language_1_Name", languageList[position].languageName)
                editor.putString("Language_1_Code", languageList[position].languageCode)

                editor.apply()

            }
            if (pos == 2) {
                binding.language2TextViewInHomeFragment.text = languageList[position].languageName
                editor.putString("Language_2_Name", languageList[position].languageName)
                editor.putString("Language_2_Code", languageList[position].languageCode)
              sharedViewModel.setLanguage(languageList[position].languageName)

                editor.apply()


            }
            dialog.cancel()


        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return languageList.size
    }

    class ViewHolder(val binding: CustomLanguageDialogBinding) :
        RecyclerView.ViewHolder(binding.root)


}