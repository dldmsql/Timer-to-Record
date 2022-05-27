package com.cookandroid.finalproject

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListAdapter
import android.widget.Toast
import com.cookandroid.finalproject.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate((layoutInflater))
        setContentView(binding.root)

        /*
        * Set for the Calendar
        * */
        val cal = Calendar.getInstance()
        val cYear = cal.get(Calendar.YEAR)
        val cMonth = cal.get(Calendar.MARCH)
        val cDay = cal.get(Calendar.DAY_OF_MONTH)

        /*
        * Create to read and write in view
        * */
        val diaryArr = ArrayList<Diary>()
        lateinit var diary: Diary


        /*
        * Define variable file name
        * */
        lateinit var fName: String

        /*
        * Initialize date picker
        * */
        binding.datePicker.init(cYear, cMonth, cDay){
                view, year, month, day ->
            /*
            * Pick date
            * */
            fName = "${year}+${month+1}+${day}"
            Log.d("TAG", fName)

            /*
            * Read data from inner storage
            * */
            var readData = readDiary(fName)

            /*
            * Define variable to show or write data
            * */
            var title = binding.title.text.toString()
            var content = binding.content.text.toString()

            /*
            * Check data is existed in inner storage
            * */
//            if (exits(fName)) {
//                id = readData?.get(0)?.toInt()!!
//            }

            diary = Diary(fName, title, content)
            diary.let { diaryArr.add(it as Diary) }

            binding.title.setText(readData?.get(1))
            binding.content.setText(readData?.get(2))
        }

        /*
        * Set Event Listener to submit button
        * */
        binding.btnSmt.setOnClickListener {
            openFileOutput(fName, Context.MODE_PRIVATE).use{
                val writeData = "${diary.id}/${diary.title}/${diary.content}"
                it.write(writeData.toByteArray())
                Toast.makeText(this,"$fName 저장", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun exits(fName: String) : Boolean {

        var ruthere : String? = null
        var result = false
        openFileInput(fName).bufferedReader().forEachLine {
            result = ruthere == null
        }
        return result
    }

    /*
    * Read data from inner storage
    * */
    private fun readDiary(fName: String): List<String>? {
        var diaryStr : String?=null
        try {
            openFileInput(fName).bufferedReader().forEachLine {
                if(diaryStr == null){
                    diaryStr = it
                } else {
                    diaryStr+= "\n$it/"
                }
                Log.d("TAG", diaryStr!!)
                binding.btnSmt.text="수정 하기"
            }
        } catch (e: IOException){
            binding.content.hint = "일기 없음"
            binding.btnSmt.text = "새로 저장"
        }
        var strs = diaryStr?.split("/")
        return strs?.toList()
    }

}

/*
* Define data class for abstraction
 */
class Diary ( val id : String,  val title : String, val content: String)