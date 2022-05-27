package com.cookandroid.finalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ListAdapter
import android.widget.Toast
import com.cookandroid.finalproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    lateinit var listAdapter: MyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate((layoutInflater))
        setContentView(binding.root)

        var dateArr = Array(2){""}
        
        val arr = arrayOf() // listView에 담고 싶은 데이터

        listAdapter = ListAdapter(this, arr)


        binding.calenderView.visibility = View.VISIBLE
        binding.calenderView.setOnDateChangeListener{
            calenderView, year, month, day ->
            dateArr[0] = year.toString()
            dateArr[1] = (month+1).toString()
            dateArr[2] = day.toString()
        }

        binding.btn1.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frag_view, WriteFragment())
                .commit()

        }
        binding.btn2.setOnClickListener {
            // main으로 돌아오기

        }
        binding.btn3.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frag_view, ShowFragment())
                .commit()

        }
        binding.fragView.setOnClickListener{
            Toast.makeText(this, "click!", Toast.LENGTH_LONG).show()
        }
    }

}