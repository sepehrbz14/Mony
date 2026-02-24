CREATE TABLE IF NOT EXISTS signup_challenges (
    id TEXT PRIMARY KEY,
    phone TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    otp_hash TEXT NOT NULL,
    remaining_attempts INTEGER NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_signup_challenges_phone ON signup_challenges(phone);
CREATE INDEX IF NOT EXISTS idx_signup_challenges_expires_at ON signup_challenges(expires_at);
