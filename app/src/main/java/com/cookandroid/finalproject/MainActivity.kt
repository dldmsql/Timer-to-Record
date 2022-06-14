package com.cookandroid.finalproject

import android.annotation.SuppressLint
import android.content.Context
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.finalproject.databinding.ActivityMainBinding
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private val soundPool = SoundPool.Builder().build() // soundPool 선언

    private var currentCountDownTimer: CountDownTimer? = null

    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null

    private var time : String? = "00:00"
    private var title : String? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindViews()
        setOnClickListener()
        initSounds() // soundPool 사용
    }

    override fun onResume() {
        super.onResume()
        // 앱이 다시 시작되는 경우
        soundPool.autoResume()
    }

    override fun onPause() {
        super.onPause()
        // 앱이 화면에 보이지 않을 경우
        soundPool.autoPause() // 모든 활성 스트림 정지
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release() // 더이상 필요 없으면 사운드풀 메모리에서 해제
    }

    private fun initSounds() {
        // sound 로드
        tickingSoundId = soundPool.load(this, R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this, R.raw.timer_bell, 1)
    }

    private fun bindViews() {
        // 각각의 뷰에대한 리스너와 코드를 연결
        binding.seekBar.setOnSeekBarChangeListener(
            /**
             * object 로 SeekBar 선언 시, 클래스 선언과 동시에 객체가 생성
             * 이는 곧 다른 class 를 상속하거나 interface 구현 가능
             * */
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    /**
                     * updateSeekBar 에서 변경되는 경우도 있기때문에 유저가 만질때만.
                       프로그레스바를 조정하고 있으면 초를 0으로 맞춰주기 위해 추가 (텍스트뷰 갱신)
                     * */
                    if (fromUser) {
                        updateRemainTimes(progress * 60 * 1000L)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // 조정하기 시작하면 기존 타이머가 있을 때 cancel 후 null
                    stopCountDown()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar ?: return

                    if (seekBar.progress == 0) {
                        stopCountDown()
                    } else {
                        startCountDown()
                    }
                }
            }
        )

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setOnClickListener(){
        binding.button25min.setOnClickListener {
            val minutes25 = 25*1000*60L // 현재 버튼 1개라 일단 이렇게 구현.

            stopCountDown()

            updateSeekBar(minutes25)
            updateRemainTimes(minutes25)

            startCountDown()
        }

        binding.buttonStop.setOnClickListener {
            stopCountDown()
        }
        binding.buttonStore.setOnClickListener {
            showStoreDialog()
        }

        binding.buttonSearch.setOnClickListener {
            showSearchDialog()
        }

    }

    private fun startCountDown() {
        // 사용자가 바에서 손을 떼는 순간 새로운 타이머 생성
        currentCountDownTimer = createCountDownTimer(binding.seekBar.progress * 60 * 1000L)
        currentCountDownTimer?.start()
        time = "${binding.remainMinutesTextView.text}:${binding.remainSecondsTextView.text}"
        /**
         * 소리 재생 (null 아닌 경우 사운드 재생)
           디바이스 자체에 요청하는 거기 때문에 화면 종료시 계속 재생될 수 있음
           생명주기이 따라 처리 필요.
         * */
        tickingSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, -1, 1F)
        }
    }

    private fun stopCountDown() {
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null
        soundPool.autoPause()
        updateRemainTimes(0)
        updateSeekBar(0)
    }

    // 타이머 생성 함수
    private fun createCountDownTimer(initialMillis: Long): CountDownTimer =
        object : CountDownTimer(initialMillis, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                updateRemainTimes(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }
        }

    private fun completeCountDown() {
        updateRemainTimes(0)
        updateSeekBar(0)

        soundPool.autoPause()
        bellSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F)
        }
    }

    // 초가 지날 때마다 텍스트뷰 갱신
    @SuppressLint("SetTextI18n")
    private fun updateRemainTimes(remainMillis: Long) {
        val remainSeconds = remainMillis / 1000

        binding.remainMinutesTextView.text = "%02d'".format(remainSeconds / 60)
        binding.remainSecondsTextView.text = "%02d".format(remainSeconds % 60)
    }

    private fun updateSeekBar(remainMillis: Long) {
        binding.seekBar.progress = (remainMillis / 1000 / 60).toInt() // 분
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showStoreDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog, null)
        val dialogText = dialogView.findViewById<EditText>(R.id.dialogEt)

        builder.setView(dialogView)
            .setPositiveButton("확인") { dialogInterface, i ->
                title = dialogText.text.toString()
                /* 확인일 때 main의 View의 값에 dialog View에 있는 값을 적용 */

                /**
                 * 오늘날짜 + 제목을 기준으로 파일에 저장
                 * */
                var now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                openFileOutput("${now}_${title}", Context.MODE_PRIVATE).use {
                    it.write(time.toString().toByteArray())
                    Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show()
                }

            }
            .setNegativeButton("취소") { dialogInterface, i ->
                /* 취소일 때 아무 액션이 없으므로 빈칸 */
            }
            .show()
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog, null)
        val dialogText = dialogView.findViewById<EditText>(R.id.dialogEt)

        builder.setView(dialogView)
            .setPositiveButton("확인") { dialogInterface, i ->
                title = dialogText.text.toString()
                /* 확인일 때 main의 View의 값에 dialog View에 있는 값을 적용 */

                /**
                 * 오늘날짜 + 제목을 기준으로 파일에서 검색
                 * */
                var now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                var readData: String? = null
                try {
                    openFileInput("${now}_${title}").bufferedReader().forEachLine {
                        if (readData == null) {
                            readData = it
                        } else {
                            readData += "\n" + it
                        }
                        Toast.makeText(this, "읽은 내용 : $readData", Toast.LENGTH_SHORT).show()
                    }
                } catch (e : IOException) {
                    Toast.makeText(this, "해당 검색어로 기록한 오늘 데이터가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소") { dialogInterface, i ->
                /* 취소일 때 아무 액션이 없으므로 빈칸 */
            }
            .show()
    }
}