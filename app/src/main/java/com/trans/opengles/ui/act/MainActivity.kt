package com.trans.opengles.ui.act

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.trans.opengles.R
import com.trans.opengles.ui.ListsFragment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragmentManager.beginTransaction()
            .add(R.id.fl_container, ListsFragment())
            .commit()
    }
}
