const { Pool } = require("pg");
const path = require("path");

require("dotenv").config({ path: path.resolve(__dirname, ".env") });

const connectionString =
  process.env.SUPABASE_DB_URL ||
  process.env.DATABASE_URL ||
  process.env.POSTGRES_URL ||
  process.env.POSTGRES_CONNECTION_STRING ||
  process.env.PG_CONNECTION_STRING;

/**
 * Phát hiện host Supabase / Pooler để tự động bật SSL.
 * Khớp với: *.supabase.co, *.supabase.com, pooler.supabase.com,
 *           aws-*-*.pooler.supabase.com, v.v.
 */
function shouldUseSsl(value) {
  if (!value) return false;
  if (process.env.PGSSLMODE === "require") return true;
  return /supabase\.(co|com)|pooler\.supabase\.com/i.test(value);
}

const SSL_CONFIG = { rejectUnauthorized: false };

const POOL_DEFAULTS = {
  max: Number(process.env.PG_POOL_MAX || 5),
  // Giảm tải cho Supabase Pooler (Transaction mode)
  idleTimeoutMillis: Number(process.env.PG_IDLE_TIMEOUT || 30000),
  // Tăng timeout kết nối – tránh EAUTHTIMEOUT trên Cloud
  connectionTimeoutMillis: Number(process.env.PG_CONNECT_TIMEOUT || 20000),
};

function buildPoolConfig() {
  if (connectionString) {
    const useSsl = shouldUseSsl(connectionString);
    console.log(`[DB] Dùng connectionString – SSL: ${useSsl}`);
    return {
      connectionString,
      ssl: useSsl ? SSL_CONFIG : undefined,
      ...POOL_DEFAULTS,
    };
  }

  const required = ["PG_USER", "PG_HOST", "PG_DATABASE", "PG_PASSWORD", "PG_PORT"];
  const missing = required.filter((key) => !process.env[key]);
  if (missing.length > 0) {
    throw new Error(
      `Missing PostgreSQL/Supabase database env vars: ${missing.join(", ")}. ` +
      "Provide SUPABASE_DB_URL/DATABASE_URL or the PG_* variables."
    );
  }

  const host = process.env.PG_HOST;
  const useSsl = shouldUseSsl(host);
  console.log(`[DB] Dùng PG_* config – host: ${host} – SSL: ${useSsl}`);
  return {
    user: process.env.PG_USER,
    host,
    database: process.env.PG_DATABASE,
    password: process.env.PG_PASSWORD,
    port: Number(process.env.PG_PORT),
    ssl: useSsl ? SSL_CONFIG : (process.env.PGSSLMODE === "require" ? SSL_CONFIG : undefined),
    ...POOL_DEFAULTS,
  };
}

const pool = new Pool(buildPoolConfig());

// Log lỗi pool-level (không crash server)
pool.on("error", (err) => {
  console.error("[DB] Pool error (idle client):", err.message);
});

// Kiểm tra kết nối lúc khởi động
pool.connect((err, client, release) => {
  if (err) {
    console.error("[DB] Lỗi kết nối PostgreSQL/Supabase:", err.message);
    console.error("[DB] Hint: Kiểm tra thông tin .env và kết nối mạng.");
    return;
  }
  client.query("SELECT NOW() AS now", (qErr, result) => {
    release();
    if (qErr) {
      console.error("[DB] Kết nối OK nhưng query thất bại:", qErr.message);
    } else {
      console.log("[DB] Kết nối thành công – Server time:", result.rows[0].now);
    }
  });
});

module.exports = pool;
