CREATE TABLE adrese (
  idAdresa integer NOT NULL 
                PRIMARY KEY GENERATED ALWAYS AS IDENTITY 
                (START WITH 1, INCREMENT BY 1),
  adresa varchar(255) NOT NULL DEFAULT '' UNIQUE,
  latitude varchar(25) NOT NULL DEFAULT '',
  longitude varchar(25) NOT NULL DEFAULT ''
);

CREATE TABLE thop_meteo (
  idthop_meteo integer NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	vlaga varchar(255) NOT NULL DEFAULT '',
	vjetar varchar(255) NOT NULL DEFAULT '',
	vrijeme varchar(255) NOT NULL DEFAULT '',
	hPa varchar(25) NOT NULL DEFAULT '',
	temperatura varchar(25) NOT NULL DEFAULT '',
	idAdresa integer NOT NULL,
	CONSTRAINT thop_meteo_FK1 FOREIGN KEY (idAdresa) REFERENCES adrese(idAdresa)
);