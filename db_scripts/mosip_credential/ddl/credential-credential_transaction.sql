-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_credential
-- Table Name 	: credential.credential_transaction
-- Purpose    	: Credential: The credential share is a functional service that interacts with the ID Repository and collects the user attributes for printing.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Aug-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes
-- Apr-2021		Ram Bhatt	    status_comment added
-- ------------------------------------------------------------------------------------------
-- object: credential.credential_transaction | type: TABLE --
-- DROP TABLE IF EXISTS credential.credential_transaction CASCADE;
CREATE TABLE credential.credential_transaction(
	id character varying(36) NOT NULL,
	credential_id character varying(36),
	request character varying,
	status_code character varying(32) NOT NULL,
	datashareurl character varying(256),
	issuancedate timestamp,
	signature character varying,
	trn_retry_count smallint,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	status_comment character varying(512),
	CONSTRAINT pk_credtrn_id PRIMARY KEY (id)

);

CREATE INDEX cred_tran_NEW_status_cr_dtimes ON credential.credential_transaction USING btree (cr_dtimes) WHERE status_code = 'NEW';

-- ddl-end --
COMMENT ON TABLE credential.credential_transaction IS 'Credential: The credential share is a functional service that interacts with the ID Repository and collects the user attributes for printing';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.id IS 'ID: Unique id generated by the system for each credentials generated';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.credential_id IS 'Credential Id: Credential id generated when distribute credential';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.request IS 'Request: Request json of credential request genrator';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.status_code IS 'Status Code: Contains status of request';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.datashareurl IS 'Datashare URL: Credential data url';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.issuancedate IS 'Issuance Date: Credential issue date';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.signature IS 'Signature: Signature of credential data';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.trn_retry_count IS 'Retry Count: Request retry count';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN credential.credential_transaction.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
