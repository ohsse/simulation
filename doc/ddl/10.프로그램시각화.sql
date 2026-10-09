create table pgm_vis_m (
    vis_id varchar(36),
    pgm_id varchar(36) not null,
    vis_nm varchar(50),
    vis_type_cd varchar(8),
    vis_setup_text jsonb,
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint pgm_vis_m_pk primary key (vis_id)
);
comment on table pgm_vis_m is '프로그램시각화정보';
comment on column pgm_vis_m.vis_id is '시각화ID';
comment on column pgm_vis_m.pgm_id is '프로그램ID';
comment on column pgm_vis_m.vis_nm is '시각화명';
comment on column pgm_vis_m.vis_type_cd is '시각화유형코드';
comment on column pgm_vis_m.vis_setup_text is '시각화설정';
comment on column pgm_vis_m.rgst_id is '등록ID';
comment on column pgm_vis_m.rgst_dttm is '등록일시';
comment on column pgm_vis_m.mdf_id is '수정ID';
comment on column pgm_vis_m.mdf_dttm is '수정일시';

create index pgm_vis_m_json_idx on pgm_vis_m
    using gin (vis_setup_text);