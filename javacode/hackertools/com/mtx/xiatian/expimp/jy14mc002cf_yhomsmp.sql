drop table if exists OMIM_PLAN;
-- OMIM_PLAN
CREATE TABLE OMIM_PLAN (
    PLANID VARCHAR2 (20) NOT NULL,
    LOGID VARCHAR2 (20) ,
    PLANNAME VARCHAR2 (50) ,
    PLANPERSON VARCHAR2 (50) ,
    PLANPERSONDEPART VARCHAR2 (50) ,
    PLANTIME DATE,
    PLANPERSONCONTACT VARCHAR2 (50) ,
    PLANPERSONPHONE VARCHAR2 (20) ,
    PLANPERSONADDR VARCHAR2 (200) ,
    PLANREMIND VARCHAR2 (2) ,
    PLANREMINDRULE VARCHAR2 (500) ,
    STATE VARCHAR2 (6) ,
    CREATER VARCHAR2 (20) ,
    CREATIME DATE,
    YAZ001 VARCHAR2 (20) ,
    YAB003 VARCHAR2 (12) ,
    YAB139 VARCHAR2 (12) ,
    BEFOREPLAN VARCHAR2 (2000) ,
    DEFINEID VARCHAR2 (20) ,
    PLANENDTIME DATE,
    PLANID_TEMP VARCHAR2 (20) ,
    ISSENDAUDIT VARCHAR2 (10) ,
    AUDITSEND VARCHAR2 (10) ,
    DISPOSERESULT VARCHAR2 (10) ,
    ISRULEPLANBACK VARCHAR2 (10) ,
    PRIMARY KEY (PLANID));
------
comment on table OMIM_PLAN
 is '巡检计划';
------
comment on column OMIM_PLAN.PLANID
is '计划巡检ID';
------
comment on column OMIM_PLAN.LOGID
is '业务日志ID';
------
comment on column OMIM_PLAN.PLANNAME
is '计划巡检名称';
------
comment on column OMIM_PLAN.PLANPERSON
is '计划巡检人';
------
comment on column OMIM_PLAN.PLANPERSONDEPART
is '计划巡检人所属机构';
------
comment on column OMIM_PLAN.PLANTIME
is '计划巡检时间';
------
comment on column OMIM_PLAN.PLANPERSONCONTACT
is '计划巡检人联系方式';
------
comment on column OMIM_PLAN.PLANPERSONPHONE
is '计划巡检人电话';
------
comment on column OMIM_PLAN.PLANPERSONADDR
is '计划巡检人地址';
------
comment on column OMIM_PLAN.PLANREMIND
is '是否纳入系统提醒';
------
comment on column OMIM_PLAN.PLANREMINDRULE
is '系统提醒规则';
------
comment on column OMIM_PLAN.STATE
is '状态';
------
comment on column OMIM_PLAN.CREATER
is '创建人';
------
comment on column OMIM_PLAN.CREATIME
is '创建时间';
------
comment on column OMIM_PLAN.YAZ001
is '业务受理号';
------
comment on column OMIM_PLAN.YAB003
is '分中心';
------
comment on column OMIM_PLAN.YAB139
is '数据区';
------
comment on column OMIM_PLAN.BEFOREPLAN
is '计划巡检前置条件';
------
comment on column OMIM_PLAN.DEFINEID
is '常规巡检计划ID';
------
comment on column OMIM_PLAN.PLANENDTIME
is '预计结束时间';
------
comment on column OMIM_PLAN.PLANID_TEMP
is '巡检计划编号—临时';
------
comment on column OMIM_PLAN.ISSENDAUDIT
is '是否送审';
------
comment on column OMIM_PLAN.AUDITSEND
is '审核是否送审';
------
comment on column OMIM_PLAN.DISPOSERESULT
is '巡检送审处理结果';
------
comment on column OMIM_PLAN.ISRULEPLANBACK
is '是否常规巡检不通过返回实施';
------
INSERT INTO OMIM_PLAN VALUES ('100000000000000761', '10000003847', '123', 'developer', '四川省', to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), '1', '0', NULL, '1', NULL, 'S01', '1', to_date('2016-04-01 11:58:46','YYYY-MM-DD HH24:MI:SS'), NULL, '510000', NULL, '123', NULL, to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), NULL, NULL, NULL, NULL, NULL);
------
INSERT INTO OMIM_PLAN VALUES ('100000000000000762', '10000003848', '123', 'developer', '四川省', to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), '1', '0', NULL, '1', NULL, 'S15', '1', to_date('2016-02-01 11:58:46','YYYY-MM-DD HH24:MI:SS'), NULL, '510100', NULL, '123', NULL, to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), NULL, NULL, NULL, NULL, NULL);
------
INSERT INTO OMIM_PLAN VALUES ('100000000000000763', '10000003849', '123', 'developer', '四川省', to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), '1', '0', NULL, '1', NULL, 'S031', '1', to_date('2016-02-01 11:58:46','YYYY-MM-DD HH24:MI:SS'), NULL, '510100', NULL, '123', NULL, to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), NULL, NULL, NULL, NULL, NULL);
------
INSERT INTO OMIM_PLAN VALUES ('100000000000000764', '10000003850', '123', 'developer', '四川省', to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), '1', '0', NULL, '1', NULL, 'S15', '1', to_date('2016-02-01 11:58:46','YYYY-MM-DD HH24:MI:SS'), NULL, '510000', NULL, '123', NULL, to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), NULL, NULL, NULL, NULL, NULL);
------
INSERT INTO OMIM_PLAN VALUES ('100000000000000765', '10000003851', '123', 'developer', '四川省', to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), '1', '0', NULL, '1', NULL, 'S051', '1', to_date('2016-02-01 11:58:46','YYYY-MM-DD HH24:MI:SS'), NULL, '510101', NULL, '123', NULL, to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), NULL, NULL, NULL, NULL, NULL);
------
INSERT INTO OMIM_PLAN VALUES ('100000000000000766', '10000003852', '123', 'developer', '四川省', to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), '1', '0', NULL, '1', NULL, 'S01', '1', to_date('2016-02-01 11:58:46','YYYY-MM-DD HH24:MI:SS'), NULL, '510000', NULL, '123', NULL, to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), NULL, NULL, NULL, NULL, NULL);
------
INSERT INTO OMIM_PLAN VALUES ('100000000000000767', '10000003853', '123', 'developer', '四川省', to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), '1', '0', NULL, '1', NULL, 'S01', '1', to_date('2016-02-01 11:58:46','YYYY-MM-DD HH24:MI:SS'), NULL, '510101', NULL, '123', NULL, to_date('2016-02-01 12:00:12','YYYY-MM-DD HH24:MI:SS'), NULL, NULL, NULL, NULL, NULL);
------
commit;
------
