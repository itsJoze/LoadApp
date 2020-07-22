package com.udacity


import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.udacity.utils.NotificationUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var mDownloadManager: DownloadManager
    private var mFileIds = mutableSetOf<Long>()
    private var mFilesDownloaded = 0

    enum class DownloadStatus {
        FAIL,
        SUCCESS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(brReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(
                this,
                NotificationUtils.downloadsChannel(this)
            )
        }

        btnListeners()
    }


    private fun btnListeners() {
        btn_download.setOnClickListener {
            when (rg_list.checkedRadioButtonId) {
                R.id.rb_loadApp -> {
                    downloadFile(URL_LOADAPP)
                    btn_download.setState(LoadingButton.State.LOADING)
                }
                R.id.rb_glide -> {
                    downloadFile(URL_GLIDE)
                    btn_download.setState(LoadingButton.State.LOADING)
                }
                R.id.rb_retrofit -> {
                    downloadFile(URL_RETROFIT)
                    btn_download.setState(LoadingButton.State.LOADING)
                }
                else -> {
                    Toast.makeText(
                        this,
                        getString(R.string.choose),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private val brReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (mFileIds.remove(downloadId)) {
                if (--mFilesDownloaded == 0) {
                    btn_download.setState(LoadingButton.State.COMPLETE)
                }

                val extras = intent!!.extras
                val query = DownloadManager.Query()
                    .setFilterById(extras!!.getLong(DownloadManager.EXTRA_DOWNLOAD_ID))
                val cursor = mDownloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    )
                    val fileName = cursor.getString(
                        cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                    )
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            NotificationUtils.downloadNotification(
                                this@MainActivity,
                                downloadId!!.toInt(),
                                DownloadStatus.SUCCESS,
                                fileName
                            )
                        }
                        DownloadManager.STATUS_FAILED -> {
                            NotificationUtils.downloadNotification(
                                this@MainActivity,
                                downloadId!!.toInt(),
                                DownloadStatus.FAIL,
                                fileName
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(brReceiver)
    }

    private fun downloadFile(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setDescription(getString(R.string.downloading))
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        mDownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        // enqueue puts the download request in the queue.
        mFileIds.add(mDownloadManager.enqueue(request))
        mFilesDownloaded++
    }


    companion object {
        private const val URL_GLIDE = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_LOADAPP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT = "https://github.com/square/retrofit/archive/master.zip"
    }
}
