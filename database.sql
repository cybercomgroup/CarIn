drop owned by v2x;

CREATE TABLE blackboard (
       key SERIAL PRIMARY KEY,
       itemid INTEGER,
       stationid INTEGER,
       appid INTEGER,
       typeid VARCHAR,
       latitude FLOAT,
       longitude FLOAT,
       locationconfidence INTEGER,
       validityduration float,
       validityarea INTEGER --Currently unused--
);

CREATE TABLE properties (
       parent INTEGER,
       type VARCHAR,
       value INTEGER,

       PRIMARY KEY (parent, type),
       FOREIGN KEY (parent) REFERENCES blackboard
);
