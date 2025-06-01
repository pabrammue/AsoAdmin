package com.example.asoadmin.DDBB

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.Json
import java.util.Properties

class supabaseClient(private val context: Context) {
    private val props = Properties().apply {
        context.assets.open("config.properties").use { load(it) }
    }

    private val url = props.getProperty("SUPABASE_URL")
    private val key = props.getProperty("SUPABASE_KEY")

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    private val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = url,
        supabaseKey = key
    ) {
        install(Postgrest)
    }

    fun getClient(): SupabaseClient = client
}
