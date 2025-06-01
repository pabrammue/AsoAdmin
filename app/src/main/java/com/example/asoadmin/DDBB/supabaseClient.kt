package com.example.asoadmin.DDBB

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.Json

class supabaseClient(private val context: Context) {
    
    private val url = ConfigManager.getSupabaseUrl(context)
    private val key = ConfigManager.getSupabaseKey(context)

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
