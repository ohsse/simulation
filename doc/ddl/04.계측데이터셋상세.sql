create table msrm_ds_d (
    ds_itm_id varchar(36),
    ds_id varchar(36) not null,
    tag_sn varchar(20) not null,
    sort_ord integer not null,
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint msrm_ds_d_pk primary key (ds_itm_id)
);

comment on table msrm_ds_d is '계측데이터셋상세';
comment on column msrm_ds_d.ds_itm_id is '데이터셋항목ID';
comment on column msrm_ds_d.ds_id is '데이터셋ID';
comment on column msrm_ds_d.tag_sn is '태그번호';
comment on column msrm_ds_d.sort_ord is '정렬순서';
comment on column msrm_ds_d.rgst_id is '등록ID';
comment on column msrm_ds_d.rgst_dttm is '등록일시';
comment on column msrm_ds_d.mdf_id is '수정ID';
comment on column msrm_ds_d.mdf_dttm is '수정일시';