package com.titoshvily.gpstracker

import android.app.Application
import com.titoshvily.gpstracker.database.MainDb

class MainApp: Application() {
    val database by lazy { MainDb.getDataBase(this) }


}