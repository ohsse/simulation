create table user_m (
    user_id varchar(20),
    user_nm varchar(50),
    user_pwd varchar(64),
    user_email varchar(40),
    user_mblno varchar(15),
    user_telno varchar(15),
    auth_cd varchar(10),
    salt_key varchar(24),
    rftk_val varchar(200),
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint user_m_pk primary key (user_id)
);

comment on table user_m is '사용자정보';
comment on column user_m.user_id is '사용자ID';
comment on column user_m.user_nm is '사용자명';
comment on column user_m.user_pwd is '사용자비밀번호';
comment on column user_m.user_email is '사용자이메일';
comment on column user_m.user_mblno is '휴대전화번호';
comment on column user_m.user_telno is '전화번호';
comment on column user_m.auth_cd is '권한코드';
comment on column user_m.salt_key is '솔트값';
comment on column user_m.rftk_val is '리프레시토큰';
comment on column user_m.rgst_id is '등록ID';
comment on column user_m.rgst_dttm is '등록일시';
comment on column user_m.mdf_id is '수정ID';
comment on column user_m.mdf_dttm is '수정일시';