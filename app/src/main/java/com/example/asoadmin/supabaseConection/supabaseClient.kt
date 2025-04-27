package com.example.asoadmin.supabaseConection

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import java.util.Properties


class supabaseClient(private val context: Context) {
    private val properties = Properties().apply {
        val inputStream = context.assets.open("config.properties")
        load(inputStream)
        inputStream.close()
    }
 //probando cosas
    val supabase = createSupabaseClient(
        supabaseUrl = properties.getProperty("SUPABASE_URL"),
        supabaseKey = properties.getProperty("SUPABASE_KEY"),
    ) {
        install(Postgrest)
    }

    fun createClient(): SupabaseClient {
        return supabase
    }

}