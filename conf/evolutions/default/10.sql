# --- !Ups

ALTER TABLE local_user ALTER COLUMN is_admin SET DEFAULT FALSE; -- --- デフォルト値設定
UPDATE local_user SET is_admin = FALSE where is_admin IS NULL;

# --- !Downs

ALTER TABLE local_user ALTER COLUMN is_admin DROP DEFAULT;
