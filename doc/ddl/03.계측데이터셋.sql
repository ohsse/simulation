create table msrm_ds_m (
    ds_id varchar(36),
    rltm_yn char(1),
    strt_dttm timestamp,
    end_dttm timestamp,
    term_type_cd varchar(5),
    inqy_term integer,
    constraint msrm_ds_m_pk primary key (ds_id)
);
comment on table msrm_ds_m is '계측데이터셋정보';
comment on column msrm_ds_m.ds_id is '데이터셋ID';
comment on column msrm_ds_m.rltm_yn is '실시간여부';
comment on column msrm_ds_m.strt_dttm is '시작일시';
comment on column msrm_ds_m.end_dttm is '종료일시';
comment on column msrm_ds_m.term_type_cd is '생성주기유형';
comment on column msrm_ds_m.inqy_term is '조회기간';