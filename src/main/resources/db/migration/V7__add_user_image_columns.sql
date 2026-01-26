-- V7__add_user_image_columns.sql
ALTER TABLE users
ADD COLUMN IF NOT EXISTS profile_pic_key VARCHAR(255);
ALTER TABLE users
ADD COLUMN IF NOT EXISTS kyc_document_key VARCHAR(255);
COMMENT ON COLUMN users.profile_pic_key IS 'S3 key for the user profile picture';
COMMENT ON COLUMN users.kyc_document_key IS 'S3 key for the user KYC document';