-- Migration: Service / Asset catalog and room assignment tables.
-- Designed for Supabase PostgreSQL.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    price NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (price >= 0),
    unit TEXT,
    icon TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    price NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (price >= 0),
    unit TEXT,
    icon TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS room_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id INTEGER NOT NULL REFERENCES phong(id_phong) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    price NUMERIC(15, 2) NOT NULL CHECK (price >= 0),
    total_price NUMERIC(15, 2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS room_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id INTEGER NOT NULL REFERENCES phong(id_phong) ON DELETE CASCADE,
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    price NUMERIC(15, 2) NOT NULL CHECK (price >= 0),
    total_price NUMERIC(15, 2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_services_name_lower ON services (LOWER(name));
CREATE INDEX IF NOT EXISTS idx_assets_name_lower ON assets (LOWER(name));
CREATE INDEX IF NOT EXISTS idx_room_services_room_id ON room_services(room_id);
CREATE INDEX IF NOT EXISTS idx_room_assets_room_id ON room_assets(room_id);

DROP TRIGGER IF EXISTS trg_services_updated_at ON services;
CREATE TRIGGER trg_services_updated_at
    BEFORE UPDATE ON services
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_assets_updated_at ON assets;
CREATE TRIGGER trg_assets_updated_at
    BEFORE UPDATE ON assets
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_room_services_updated_at ON room_services;
CREATE TRIGGER trg_room_services_updated_at
    BEFORE UPDATE ON room_services
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_room_assets_updated_at ON room_assets;
CREATE TRIGGER trg_room_assets_updated_at
    BEFORE UPDATE ON room_assets
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
