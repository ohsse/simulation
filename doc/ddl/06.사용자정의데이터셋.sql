create table udf_ds_m (
    ds_id varchar(36),
    constraint udf_ds_m_pk primary key (ds_id)
);

comment on table udf_ds_m is '사용자정의데이터셋';
comment on column udf_ds_m.ds_id is '데이터셋ID';