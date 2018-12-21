drop owned by v2x;

CREATE TABLE blackboard (
       key SERIAL PRIMARY KEY,
       itemid VARCHAR,
       stationid INTEGER,
       appid INTEGER,
       typeid VARCHAR,
       latitude FLOAT,
       longitude FLOAT,
       locationconfidence FLOAT,
       validityduration FLOAT,
       validityarea INTEGER --Currently unused--
);

CREATE TABLE properties (
       parent INTEGER,
       type VARCHAR,
       value VARCHAR,

       PRIMARY KEY (parent, type),
       FOREIGN KEY (parent) REFERENCES blackboard
);
