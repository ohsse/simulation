create table venv_m (
    venv_id varchar(36),
    venv_nm varchar(50),
    venv_desc varchar(100),
    py_vrsn varchar(10),
    use_able_yn char(1),
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint venv_m_pk primary key (venv_id)
);

comment on table venv_m is '가상환경정보';
comment on column venv_m.venv_id is '가상환경ID';
comment on column venv_m.venv_nm is '가상환경명';
comment on column venv_m.venv_desc is '가상환경설명';
comment on column venv_m.py_vrsn is '파이썬버전';
comment on column venv_m.use_able_yn is '사용가능여부';
comment on column venv_m.rgst_id is '등록ID';
comment on column venv_m.rgst_dttm is '등록일시';
comment on column venv_m.mdf_id is '수정ID';
comment on column venv_m.mdf_dttm is '수정일시';