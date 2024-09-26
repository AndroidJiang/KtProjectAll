package com.example.ktprojectall.kotlin.ui.provider

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.ktprojectall.R
import com.google.gson.Gson
import java.io.File


class ProviderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider)
        val img = findViewById<ImageView>(R.id.img_provider)
//        var imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val imageFile = File("/storage/emulated/0/Android/data/com.example.myapplication/files/Pictures/image.jpg")
        val authority = "com.example.myapplication.fileprovider" // A 应用的 FileProvider authorities
        val imageFileName = "image.jpg" // 知道文件的名字
        val imageUri = Uri.parse("content://$authority/external_files/$imageFileName")

        // 1. 使用FileProvider获取可共享的URI
//        val imageUri = FileProvider.getUriForFile(
//            this@ProviderActivity,
//            "com.example.myapplication.fileprovider",  // 需要在AndroidManifest.xml中定义
//            imageFile
//        )
//        grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        if (imageUri != null) {
//            img.setImageURI(imageUri)
//        }

        val gson = Gson()
        val serverUser = ServerUser("AAAA", 33.1);
        val toJson = gson.toJson(serverUser)
        val user = gson.fromJson<User>(toJson, User::class.java)
        Log.e("ajiang",user.toString())
    }
}