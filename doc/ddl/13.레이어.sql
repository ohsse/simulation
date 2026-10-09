create table layer_m (
    layer_id varchar(36),
    layer_nm varchar(50),
    layer_desc varchar(100),
    init_dspy_yn char(1),
    crsy_type_cd varchar(15),
    layer_styles jsonb,
    use_able_yn char(1),
    layer_ftype varchar(8),
    grp_id varchar(36),
    sort_ord integer,
    properties jsonb,
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint layer_m_pk primary key (layer_id)
);

comment on table layer_m is '레이어기본정보';
comment on column layer_m.layer_id is '레이어ID';
comment on column layer_m.layer_nm is '레이어명';
comment on column layer_m.layer_desc is '레이어설명';
comment on column layer_m.init_dspy_yn is '최초표시여부';
comment on column layer_m.crsy_type_cd is '좌표계정보';
comment on column layer_m.layer_styles is '레이어스타일설정';
comment on column layer_m.use_able_yn is '사용가능여부';
comment on column layer_m.layer_ftype is '레이어객체유형';
comment on column layer_m.grp_id is '그룹ID';
comment on column layer_m.sort_ord is '정렬순서';
comment on column layer_m.properties is '프로퍼티목록';
comment on column layer_m.rgst_id is '등록ID';
comment on column layer_m.rgst_dttm is '등록일시';
comment on column layer_m.mdf_id is '수정ID';
comment on column layer_m.mdf_dttm is '수정일시';