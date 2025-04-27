package com.example.asoadmin.supabaseConection

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://bpqcdxhrzwtzfnppmxla.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJwcWNkeGhyend0emZucHBteGxhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQzMjAyMjQsImV4cCI6MjA1OTg5NjIyNH0.meASDNvCYsL4Rmxi6wagjKQLzBzgMe24PUL3guW6Gv0"
) {
    install(Postgrest)
}


class supabaseClient {

}