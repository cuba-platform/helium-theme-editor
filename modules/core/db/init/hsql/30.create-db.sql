------------------------------------------------------------------------------------------------------------
-- Demo UserRole
------------------------------------------------------------------------------------------------------------

insert into SEC_USER_ROLE
(ID, VERSION, CREATE_TS, USER_ID, ROLE_ID, ROLE_NAME)
values ('2f7f101a-6c92-13eb-c900-36fead92f60b', 1, current_timestamp, 'a405db59-e674-4f63-8afe-269dda788fe8', null, 'demo');

------------------------------------------------------------------------------------------------------------
-- Changing administrator password
------------------------------------------------------------------------------------------------------------
update SEC_USER
set PASSWORD='$2a$10$BXl/gTmIhSQFMRCzCMXTSuvYV60tfw1Ngzrd2IMlG.5Q3okUkeLem', PASSWORD_ENCRYPTION='bcrypt'
where ID='60885987-1b61-4247-94c7-dff348347f93';