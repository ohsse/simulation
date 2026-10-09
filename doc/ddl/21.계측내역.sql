create table msrm_l (
    tag_sn varchar(20),
    msrm_dttm timestamp,
    msrm_val numeric,
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint msrm_l_pk primary key (tag_sn, msrm_dttm)
) partition by range (msrm_dttm);

create table msrm_l_p202501 partition of msrm_l
    for values from ('2025-01-01 00:00:00') to ('2025-02-01 00:00:00')
    partition by hash(tag_sn);

create table msrm_l_p202501_0 partition of msrm_l_p202501
    for values with (modulus 8, remainder 0);
create table msrm_l_p202501_1 partition of msrm_l_p202501
    for values with (modulus 8, remainder 1);
create table msrm_l_p202501_2 partition of msrm_l_p202501
    for values with (modulus 8, remainder 2);
create table msrm_l_p202501_3 partition of msrm_l_p202501
    for values with (modulus 8, remainder 3);
create table msrm_l_p202501_4 partition of msrm_l_p202501
    for values with (modulus 8, remainder 4);
create table msrm_l_p202501_5 partition of msrm_l_p202501
    for values with (modulus 8, remainder 5);
create table msrm_l_p202501_6 partition of msrm_l_p202501
    for values with (modulus 8, remainder 6);
create table msrm_l_p202501_7 partition of msrm_l_p202501
    for values with (modulus 8, remainder 7);

comment on table msrm_l is '계측내역';
comment on column msrm_l.tag_sn is '태그번호';
comment on column msrm_l.msrm_dttm is '계측일시';
comment on column msrm_l.msrm_val is '계측값';
comment on column msrm_l.rgst_id is '등록ID';
comment on column msrm_l.rgst_dttm is '등록일시';
comment on column msrm_l.mdf_id is '수정ID';
comment on column msrm_l.mdf_dttm is '수정일시';