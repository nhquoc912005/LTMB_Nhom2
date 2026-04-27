const { Pool } = require("pg");
const path = require("path");

require("dotenv").config({ path: path.resolve(__dirname, ".env") });

const connectionString =
  process.env.SUPABASE_DB_URL ||
  process.env.DATABASE_URL ||
  process.env.POSTGRES_URL ||
  process.env.POSTGRES_CONNECTION_STRING ||
  process.env.PG_CONNECTION_STRING;

function extractHostname(value) {
  if (!value) return "";
  try {
    if (/^postgres(ql)?:\/\//i.test(value)) {
      return new URL(value).hostname || "";
    }
  } catch (_) {
    return "";
  }
  return String(value).trim();
}

function shouldUseSsl(value) {
  const host = extractHostname(value);
  return process.env.PGSSLMODE === "require" ||
    /supabase\.(co|com)$/i.test(host) ||
    /(^|[.-])pooler([.-]|$)/i.test(host);
}

function buildPoolConfig() {
  const timeoutConfig = {
    connectionTimeoutMillis: 20000,
    authenticationTimeout: 20000,
    max: Number(process.env.PG_POOL_MAX || 10),
  };

  if (connectionString) {
    return {
      connectionString,
      ssl: shouldUseSsl(connectionString) ? { rejectUnauthorized: false } : undefined,
      ...timeoutConfig,
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
    ssl: shouldUseSsl(process.env.PG_HOST) ? { rejectUnauthorized: false } : undefined,
    ...timeoutConfig,
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
