create table pgm_exec_h (
    hist_id varchar(36) ,
    pgm_id varchar(36) not null,
    exec_strt_dttm timestamp,
    exec_end_dttm timestamp,
    exec_type_cd varchar(10),
    exec_stts_cd varchar(10),
    procs_id varchar(10),
    rslt_dir_id varchar(36),
    rslt_bytes bigint,
    err_text text,
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint pgm_exec_h_pk primary key (hist_id, exec_strt_dttm, pgm_id)
) partition by range (exec_strt_dttm);

create table pgm_exec_h_p202501 partition of pgm_exec_h
for values from ('2025-01-01 00:00:00') to ('2025-02-01 00:00:00')
partition by hash (pgm_id);

CREATE TABLE pgm_exec_h_p202501_0 PARTITION OF pgm_exec_h_p202501 
    FOR VALUES WITH (modulus 8, remainder 0);
CREATE TABLE pgm_exec_h_p202501_1 PARTITION OF pgm_exec_h_p202501
    FOR VALUES WITH (modulus 8, remainder 1);
CREATE TABLE pgm_exec_h_p202501_2 PARTITION OF pgm_exec_h_p202501
    FOR VALUES WITH (modulus 8, remainder 2);
CREATE TABLE pgm_exec_h_p202501_3 PARTITION OF pgm_exec_h_p202501
    FOR VALUES WITH (modulus 8, remainder 3);
CREATE TABLE pgm_exec_h_p202501_4 PARTITION OF pgm_exec_h_p202501
    FOR VALUES WITH (modulus 8, remainder 4);
CREATE TABLE pgm_exec_h_p202501_5 PARTITION OF pgm_exec_h_p202501
    FOR VALUES WITH (modulus 8, remainder 5);
CREATE TABLE pgm_exec_h_p202501_6 PARTITION OF pgm_exec_h_p202501
    FOR VALUES WITH (modulus 8, remainder 6);
CREATE TABLE pgm_exec_h_p202501_7 PARTITION OF pgm_exec_h_p202501
    FOR VALUES WITH (modulus 8, remainder 7);

comment on table pgm_exec_h is '프로그램실행이력';
comment on column pgm_exec_h.hist_id is '이력ID';
comment on column pgm_exec_h.pgm_id is '프로그램ID';
comment on column pgm_exec_h.exec_strt_dttm is '실행시작일시';
comment on column pgm_exec_h.exec_end_dttm is '실행종료일시';
comment on column pgm_exec_h.exec_type_cd is '실행유형코드';
comment on column pgm_exec_h.exec_stts_cd is '실행상태코드';
comment on column pgm_exec_h.procs_id is '프로세스ID';
comment on column pgm_exec_h.rslt_dir_id is '결과폴더ID';
comment on column pgm_exec_h.rslt_bytes is '결과용량';
comment on column pgm_exec_h.err_text is '에러내용';
comment on column pgm_exec_h.rgst_id is '등록ID';
comment on column pgm_exec_h.rgst_dttm is '등록일시';
comment on column pgm_exec_h.mdf_id is '수정ID';
comment on column pgm_exec_h.mdf_dttm is '수정일시';

create index pgm_exec_h_idx_01 on pgm_exec_h
(pgm_id, rslt_dir_id);