create table wnet_tag (
    tag_sn varchar(20),
    tag_se_cd varchar(5),
    tag_desc varchar(100),
    use_yn char(1) default 'Y',
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint wnet_tag_pk primary key (tag_sn)
);

comment on table wnet_tag is '워터넷태그정보';
comment on column wnet_tag.tag_sn is '태그번호';
comment on column wnet_tag.tag_se_cd is '태그유형코드';
comment on column wnet_tag.tag_desc is '태그설명';
comment on column wnet_tag.use_yn is '사용여부(수집여부)';
comment on column wnet_tag.rgst_id is '등록ID';
comment on column wnet_tag.rgst_dttm is '등록일시';
comment on column wnet_tag.mdf_id is '수정ID';
comment on column wnet_tag.mdf_dttm is '수정일시';