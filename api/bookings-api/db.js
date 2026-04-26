const { Pool } = require("pg");
const path = require("path");

require("dotenv").config({ path: path.resolve(__dirname, ".env") });

const connectionString =
  process.env.SUPABASE_DB_URL ||
  process.env.DATABASE_URL ||
  process.env.POSTGRES_URL ||
  process.env.POSTGRES_CONNECTION_STRING ||
  process.env.PG_CONNECTION_STRING;

function shouldUseSsl(value) {
  return process.env.PGSSLMODE === "require" || /supabase\.(co|com)|pooler\.supabase\.com/i.test(value);
}

function buildPoolConfig() {
  if (connectionString) {
    return {
      connectionString,
      ssl: shouldUseSsl(connectionString) ? { rejectUnauthorized: false } : undefined,
      max: Number(process.env.PG_POOL_MAX || 10),
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

  return {
    user: process.env.PG_USER,
    host: process.env.PG_HOST,
    database: process.env.PG_DATABASE,
    password: process.env.PG_PASSWORD,
    port: Number(process.env.PG_PORT),
    ssl: process.env.PGSSLMODE === "require" ? { rejectUnauthorized: false } : undefined,
    max: Number(process.env.PG_POOL_MAX || 10),
  };
}

const pool = new Pool(buildPoolConfig());

pool.connect((err, client, release) => {
  if (err) {
    console.error("Lỗi kết nối PostgreSQL/Supabase:", err.message);
    return;
  }
  console.log("Đã kết nối PostgreSQL/Supabase thành công");
  release();
});

module.exports = pool;
