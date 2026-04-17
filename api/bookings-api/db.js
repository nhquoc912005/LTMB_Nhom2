const { Pool } = require("pg");
const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '.env') });

const pool = new Pool({
  user: process.env.PG_USER || 'postgres',
  host: process.env.PG_HOST || 'localhost',
  database: process.env.PG_DATABASE || 'booking_db',
  password: process.env.PG_PASSWORD || '',
  port: Number(process.env.PG_PORT) || 5432,
});

pool.connect((err, client, release) => {
  if (err) {
    console.error('❌ LỖI KẾT NỐI POSTGRESQL:', err.stack);
    console.log('Gợi ý: Hãy chắc chắn PostgreSQL đã được bật và Database "booking_db" đã tồn tại.');
  } else {
    console.log('✅ ĐÃ KẾT NỐI POSTGRESQL THÀNH CÔNG');
    release();
  }
});

module.exports = pool;
