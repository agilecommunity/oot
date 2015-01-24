# --- !Ups

INSERT INTO local_user VALUES ('admin@localhost', 'userpass', 'admin@localhost', '$2a$10$PaGN.TVmfC4sHKsuokyFFePJIt6MIBnuOMsN.2fpOZOLLAeNr9QYC', 'お弁当', '管理者', TRUE, '2014-01-01', '2014-01-01');

# --- !Downs

DELETE FROM local_user where id = 'admin@localhost';
