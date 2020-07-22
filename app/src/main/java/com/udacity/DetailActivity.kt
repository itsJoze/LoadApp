package com.udacity


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.utils.NotificationUtils
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*


class DetailActivity : AppCompatActivity() {
    private var mFileID = -1
    private lateinit var mDownloadStatus: MainActivity.DownloadStatus
    private lateinit var mFileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        extras()
        NotificationUtils.clearNotification(this, mFileID)

        tv_fileName.text = mFileName
        tv_downloadStatus.text = if (mDownloadStatus == MainActivity.DownloadStatus.SUCCESS) {
            getString(R.string.download_completed)
        } else {
            getString(R.string.download_failed)
        }

        layout_details.transitionToEnd()
        btn_close.setOnClickListener {
            finish()
        }
    }


    private fun extras() {
        val extras = intent.extras
        extras?.let {
            mFileID = it.getInt(EXTRA_FILE_ID)
            mDownloadStatus = MainActivity.DownloadStatus.values()[it.getInt(EXTRA_STATUS)]
            mFileName = it.getString(EXTRA_FILE_NAME)!!
        }
    }


    companion object {
        private const val EXTRA_FILE_ID = "file_id"
        private const val EXTRA_STATUS = "status"
        private const val EXTRA_FILE_NAME = "file_name"

        fun withExtras(
            downloadId: Int,
            downloadStatus: MainActivity.DownloadStatus,
            fileName: String
        ): Bundle {
            return Bundle().apply {
                putInt(EXTRA_FILE_ID, downloadId)
                putInt(EXTRA_STATUS, downloadStatus.ordinal)
                putString(EXTRA_FILE_NAME, fileName)
            }
        }
    }
}
