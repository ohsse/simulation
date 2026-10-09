create table dsbd_m (
    dsbd_id varchar(36),
    dsbd_nm varchar(50),
    dsbd_desc varchar(100),
    res_width_val smallint,
    res_hgln_val smallint,
    grp_id varchar(36),
    sort_ord integer,
    dsbd_vis_items jsonb,
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint dsbd_m_pk primary key (dsbd_id)
);

comment on table dsbd_m is '대시보드정보';
comment on column dsbd_m.dsbd_id is '대시보드ID';
comment on column dsbd_m.dsbd_nm is '대시보드명';
comment on column dsbd_m.dsbd_desc is '대시보드설명';
comment on column dsbd_m.res_width_val is '해상도가로값';
comment on column dsbd_m.res_hgln_val is '해상도세로값';
comment on column dsbd_m.grp_id is '그룹ID';
comment on column dsbd_m.sort_ord is '정렬순서';
comment on column dsbd_m.dsbd_vis_items is '대시보드시각화항목';
comment on column dsbd_m.rgst_id is '등록ID';
comment on column dsbd_m.rgst_dttm is '등록일시';
comment on column dsbd_m.mdf_id is '수정ID';
comment on column dsbd_m.mdf_dttm is '수정일시';

create index dsbd_m_json_idx on dsbd_m
    using gin (dsbd_vis_items);