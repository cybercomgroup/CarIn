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

CREATE TABLE intproperties (
       parent INTEGER,
       type VARCHAR,
       value INTEGER,

       PRIMARY KEY (parent, type),
       FOREIGN KEY (parent) REFERENCES blackboard
);

CREATE TABLE boolproperties (
       parent INTEGER,
       type VARCHAR,
       value BOOL,

       PRIMARY KEY (parent, type),
       FOREIGN KEY (parent) REFERENCES blackboard
);

CREATE TABLE stringproperties (
       parent INTEGER,
       type VARCHAR,
       value VARCHAR,

       PRIMARY KEY (parent, type),
       FOREIGN KEY (parent) REFERENCES blackboard
);
