create table pgm_input_file_m (
    input_file_id varchar(36),
    pgm_id varchar(36) not null,
    trgt_id varchar(36) not null,
    trgt_type_cd varchar(20) not null,
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint pgm_input_file_m_pk primary key (input_file_id)
);
comment on table pgm_input_file_m is '프로그램인풋파일정보';
comment on column pgm_input_file_m.input_file_id is '인풋파일ID';
comment on column pgm_input_file_m.pgm_id is '프로그램ID';
comment on column pgm_input_file_m.trgt_id is '대상ID';
comment on column pgm_input_file_m.trgt_type_cd is '대상유형코드';
comment on column pgm_input_file_m.rgst_id is '등록ID';
comment on column pgm_input_file_m.rgst_dttm is '등록일시';
comment on column pgm_input_file_m.mdf_id is '수정ID';
comment on column pgm_input_file_m.mdf_dttm is '수정일시';