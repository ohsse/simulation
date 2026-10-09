create table lbr_m (
    lbr_id varchar(36),
    lbr_nm varchar(100),
    lbr_vrsn varchar(20),
    py_vrsn varchar(15),
    ortx_file_nm varchar(100),
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint lbr_m_pk primary key (lbr_id)
);
comment on table lbr_m is '라이브러리정보';
comment on column lbr_m.lbr_id is '라이브러리ID';
comment on column lbr_m.lbr_nm is '라이브러리명';
comment on column lbr_m.lbr_vrsn is '라이브러리버전';
comment on column lbr_m.py_vrsn is '파이썬버전';
comment on column lbr_m.ortx_file_nm is '원본파일명';
comment on column lbr_m.rgst_id is '등록ID';
comment on column lbr_m.rgst_dttm is '등록일시';
comment on column lbr_m.mdf_id is '수정ID';
comment on column lbr_m.mdf_dttm is '수정일시';