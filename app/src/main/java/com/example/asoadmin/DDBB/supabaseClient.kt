package com.example.asoadmin.DDBB

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class supabaseClient(private val context: Context) {
    
    private val url = ConfigManager.getSupabaseUrl(context)
    private val key = ConfigManager.getSupabaseKey(context)

    /**
     * Crea un cliente para acceder a la base de datos de Supabase.
     */
    private val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = url,
        supabaseKey = key
    ) {
        install(Postgrest)
    }

    fun getClient(): SupabaseClient = client
}
