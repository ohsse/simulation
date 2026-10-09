create table ds_m (
    ds_id varchar(36),
    ds_type_cd varchar(20),
    ds_nm varchar(50),
    ds_desc varchar(100),
    sort_ord integer,
    grp_id varchar(36),
    file_xtns varchar(5),
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint pk_ds_m primary key (ds_id)
);

comment on table ds_m is '데이터셋정보';
comment on column ds_m.ds_id is '데이터셋ID';
comment on column ds_m.ds_type_cd is '데이터셋유형코드';
comment on column ds_m.ds_nm is '데이터셋명';
comment on column ds_m.ds_desc is '데이터셋설명';
comment on column ds_m.sort_ord is '정렬순서';
comment on column ds_m.grp_id is '그룹ID';
comment on column ds_m.file_xtns is '파일확장자';
comment on column ds_m.rgst_id is '등록ID';
comment on column ds_m.rgst_dttm is '등록일시';
comment on column ds_m.mdf_id is '수정ID';
comment on column ds_m.mdf_dttm is '수정일시';