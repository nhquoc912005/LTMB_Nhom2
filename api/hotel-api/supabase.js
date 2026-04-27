const path = require("path");

require("dotenv").config({ path: path.resolve(__dirname, ".env") });

function createSupabaseClient() {
  const url = process.env.SUPABASE_URL;
  const key = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_ANON_KEY;

  if (!url || !key) {
    throw new Error("Missing SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY/SUPABASE_ANON_KEY.");
  }

  let createClient;
  try {
    ({ createClient } = require("@supabase/supabase-js"));
  } catch (error) {
    throw new Error("Install @supabase/supabase-js before using the Supabase REST/Auth client.");
  }

  return createClient(url, key, {
    auth: {
      persistSession: false,
      autoRefreshToken: false,
    },
  });
}

module.exports = {
  createSupabaseClient,
};
