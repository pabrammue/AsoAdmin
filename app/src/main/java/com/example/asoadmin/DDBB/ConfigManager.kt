package com.example.asoadmin.DDBB

import android.content.Context
import java.util.Properties

object ConfigManager {
    private var properties: Properties? = null
    
    private fun loadProperties(context: Context): Properties {
        if (properties == null) {
            properties = Properties()
            try {
                val inputStream = context.assets.open("config.properties")
                properties!!.load(inputStream)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return properties!!
    }
    
    fun getSupabaseUrl(context: Context): String {
        return loadProperties(context).getProperty("SUPABASE_URL", "")
    }
    
    fun getSupabaseKey(context: Context): String {
        return loadProperties(context).getProperty("SUPABASE_KEY", "")
    }
    
    fun getSupabasePassword(context: Context): String {
        return loadProperties(context).getProperty("SUPABASE_PASSWORD", "")
    }
    
    fun getMapsApiKey(context: Context): String {
        return loadProperties(context).getProperty("MAPS_API_KEY", "")
    }
} 