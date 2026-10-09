create table pgm_m (
    pgm_id varchar(36),
    pgm_nm varchar(50),
    pgm_desc varchar(100),
    sort_ord integer,
    grp_id varchar(36),
    venv_id varchar(36),
    rltm_yn char(1),
    rptt_intv_type_cd varchar(5),
    rptt_intv_val integer,
    strt_exec_dttm timestamp,
    pgm_args jsonb,
    fnl_exec_id varchar(36),
    fnl_pdir_id varchar(36),
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint pgm_m_pk primary key (pgm_id)
);

comment on table pgm_m is '프로그램정보';
comment on column pgm_m.pgm_id is '프로그램ID';
comment on column pgm_m.pgm_nm is '프로그램명';
comment on column pgm_m.pgm_desc is '프로그램설명';
comment on column pgm_m.sort_ord is '정렬순서';
comment on column pgm_m.grp_id is '그룹ID';
comment on column pgm_m.venv_id is '가상환경ID';
comment on column pgm_m.rltm_yn is '실시간여부';
comment on column pgm_m.rptt_intv_type_cd is '실행반복유형코드';
comment on column pgm_m.rptt_intv_val is '실행반복간격';
comment on column pgm_m.strt_exec_dttm is '최초실행일시';
comment on column pgm_m.pgm_args is '프로그램인수';
comment on column pgm_m.fnl_exec_id is '최종실행ID';
comment on column pgm_m.fnl_pdir_id is '최종프로그램폴더ID';
comment on column pgm_m.rgst_id is '등록ID';
comment on column pgm_m.rgst_dttm is '등록일시';
comment on column pgm_m.mdf_id is '수정ID';
comment on column pgm_m.mdf_dttm is '수정일시';
