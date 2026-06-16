package com.futurecode.recoverdeletedmessages.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(MyApplication.setLocale(newBase))
    }
}


//NotificationAccessDialog  //layout_permission_bottom_sheet.xml
//FolderAccessDialog //layout_storage_permission_bottom_sheet.xml