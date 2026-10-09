create table pgm_grp_m (
    grp_id varchar(36),
    grp_nm varchar(50),
    grp_desc varchar(100),
    sort_ord integer,
    up_grp_id varchar(36),
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint pgm_grp_m_pk primary key (grp_id)
);
comment on table pgm_grp_m is '프로그램그룹정보';
comment on column pgm_grp_m.grp_id is '그룹ID';
comment on column pgm_grp_m.grp_nm is '그룹명';
comment on column pgm_grp_m.grp_desc is '그룹설명';
comment on column pgm_grp_m.sort_ord is '정렬순서';
comment on column pgm_grp_m.up_grp_id is '상위그룹ID';
comment on column pgm_grp_m.rgst_id is '등록ID';
comment on column pgm_grp_m.rgst_dttm is '등록일시';
comment on column pgm_grp_m.mdf_id is '수정ID';
comment on column pgm_grp_m.mdf_dttm is '수정일시';