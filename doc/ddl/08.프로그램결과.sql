create table pgm_rslt_m (
    rslt_id varchar(36),
    pgm_id varchar(36) not null,
    rslt_nm varchar(50) not null,
    file_xtns varchar(10) not null,
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint pgm_rslt_m_pk primary key (rslt_id)
);

comment on table pgm_rslt_m is '프로그램결과';
comment on column pgm_rslt_m.rslt_id is '결과ID';
comment on column pgm_rslt_m.pgm_id is '프로그램ID';
comment on column pgm_rslt_m.rslt_nm is '결과명';
comment on column pgm_rslt_m.file_xtns is '파일확장자';
comment on column pgm_rslt_m.rgst_id is '등록ID';
comment on column pgm_rslt_m.rgst_dttm is '등록일시';
comment on column pgm_rslt_m.mdf_id is '수정ID';
comment on column pgm_rslt_m.mdf_dttm is '수정일시';