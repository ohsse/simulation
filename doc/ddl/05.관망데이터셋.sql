create table pn_ds_m (
    ds_id varchar(36),
    crsy_type_cd varchar(12),
    constraint pk_pn_ds_m primary key (ds_id)
);
comment on table pn_ds_m is '관망데이터셋정보';
comment on column pn_ds_m.crsy_type_cd is '좌표계코드';