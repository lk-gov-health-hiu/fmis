-- select concat('rename table ', table_name, ' to  temp' , ';','rename table temp to  ',  upper(table_name), ';' ) from information_schema.tables where table_schema = 'covid19';
-- 
-- 
-- concat('rename table ', table_name, ' to  temp' , ';','rename table temp to  ',  upper(table_name), ';' )
rename table apirequest to  temp;rename table temp to  APIREQUEST;
rename table aprocedure to  temp;rename table temp to  APROCEDURE;
rename table area to  temp;rename table temp to  AREA;
rename table client to  temp;rename table temp to  CLIENT;
rename table component to  temp;rename table temp to  COMPONENT;
rename table consolidatedqueryresult to  temp;rename table temp to  CONSOLIDATEDQUERYRESULT;
rename table coordinate to  temp;rename table temp to  COORDINATE;
rename table demoaccount to  temp;rename table temp to  DEMOACCOUNT;
rename table encounter to  temp;rename table temp to  ENCOUNTER;
rename table individualqueryresult to  temp;rename table temp to  INDIVIDUALQUERYRESULT;
rename table institution to  temp;rename table temp to  INSTITUTION;
rename table institutioncomponent to  temp;rename table temp to  INSTITUTIONCOMPONENT;
rename table item to  temp;rename table temp to  ITEM;
rename table movement to  temp;rename table temp to  MOVEMENT;
rename table numbers to  temp;rename table temp to  NUMBERS;
rename table observation to  temp;rename table temp to  OBSERVATION;
rename table person to  temp;rename table temp to  PERSON;
rename table phn to  temp;rename table temp to  PHN;
rename table preference to  temp;rename table temp to  PREFERENCE;
rename table prescription to  temp;rename table temp to  PRESCRIPTION;
rename table relationship to  temp;rename table temp to  RELATIONSHIP;
rename table sequence to  temp;rename table temp to  SEQUENCE;
rename table sms to  temp;rename table temp to  SMS;
rename table storedqueryresult to  temp;rename table temp to  STOREDQUERYRESULT;
rename table upload to  temp;rename table temp to  UPLOAD;
rename table userarea to  temp;rename table temp to  USERAREA;
rename table userprivilege to  temp;rename table temp to  USERPRIVILEGE;
rename table usertransaction to  temp;rename table temp to  USERTRANSACTION;
rename table webuser to  temp;rename table temp to  WEBUSER;
