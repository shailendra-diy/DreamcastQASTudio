package com.selenium.studio.db;

public class SupabaseConfig {

    public static final String SUPABASE_URL      = "https://mfsdmdtdcsyvksljolwv.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1mc2RtZHRkY3N5dmtzbGpvbHd2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAyODMyNTQsImV4cCI6MjA5NTg1OTI1NH0.xkRyyKC2fEOb_VNOIIoyLrfBbpNtBVc5boFfIoxXYQs";
    public static final String REST_URL          = SUPABASE_URL + "/rest/v1";
    public static final String AUTH_URL          = SUPABASE_URL + "/auth/v1";

    private SupabaseConfig() {}
}