package com.anwesh.uiprojects.fireballbackview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.fireballbackuiview.FireBallBackView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FireBallBackView.create(this)
    }
}
