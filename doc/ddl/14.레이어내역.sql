create table layer_l (
    layer_id varchar(36),
    ftype varchar(8),
    fid bigint,
    gmtr_val geometry,
    property jsonb,
    color_str varchar(12),
    rgst_id varchar(20) not null,
    rgst_dttm timestamp not null,
    mdf_id varchar(20) not null,
    mdf_dttm timestamp not null,
    constraint layer_l_pk primary key (layer_id, ftype, fid)
) partition by list (ftype);

create index layer_l_ftype on layer_l using btree (ftype);
create index layer_l_lid on layer_l using btree (layer_id);
create index layer_l_lid_ftype on layer_l using btree (layer_id, ftype);
create index layer_l_geom on layer_l using gist (gmtr_val);

create table layer_l_p_line partition of layer_l for values in ('LINE')
partition by hash (layer_id);

create table layer_l_p_line_0 partition of layer_l_p_line
    for values with (modulus 8, remainder 0);
create table layer_l_p_line_1 partition of layer_l_p_line
    for values with (modulus 8, remainder 1);
create table layer_l_p_line_2 partition of layer_l_p_line
    for values with (modulus 8, remainder 2);
create table layer_l_p_line_3 partition of layer_l_p_line
    for values with (modulus 8, remainder 3);
create table layer_l_p_line_4 partition of layer_l_p_line
    for values with (modulus 8, remainder 4);
create table layer_l_p_line_5 partition of layer_l_p_line
    for values with (modulus 8, remainder 5);
create table layer_l_p_line_6 partition of layer_l_p_line
    for values with (modulus 8, remainder 6);
create table layer_l_p_line_7 partition of layer_l_p_line
    for values with (modulus 8, remainder 7);

create table layer_l_p_point partition of layer_l for values in ('POINT')
partition by hash (layer_id);

create table layer_l_p_point_0 partition of layer_l_p_point
    for values with (modulus 8, remainder 0);
create table layer_l_p_point_1 partition of layer_l_p_point
    for values with (modulus 8, remainder 1);
create table layer_l_p_point_2 partition of layer_l_p_point
    for values with (modulus 8, remainder 2);
create table layer_l_p_point_3 partition of layer_l_p_point
    for values with (modulus 8, remainder 3);
create table layer_l_p_point_4 partition of layer_l_p_point
    for values with (modulus 8, remainder 4);
create table layer_l_p_point_5 partition of layer_l_p_point
    for values with (modulus 8, remainder 5);
create table layer_l_p_point_6 partition of layer_l_p_point
    for values with (modulus 8, remainder 6);
create table layer_l_p_point_7 partition of layer_l_p_point
    for values with (modulus 8, remainder 7);

create table layer_l_p_polygon partition of layer_l for values in ('POLYGON')
partition by hash (layer_id);

create table layer_l_p_polygon_0 partition of layer_l_p_polygon
    for values with (modulus 8, remainder 0);
create table layer_l_p_polygon_1 partition of layer_l_p_polygon
    for values with (modulus 8, remainder 1);
create table layer_l_p_polygon_2 partition of layer_l_p_polygon
    for values with (modulus 8, remainder 2);
create table layer_l_p_polygon_3 partition of layer_l_p_polygon
    for values with (modulus 8, remainder 3);
create table layer_l_p_polygon_4 partition of layer_l_p_polygon
    for values with (modulus 8, remainder 4);
create table layer_l_p_polygon_5 partition of layer_l_p_polygon
    for values with (modulus 8, remainder 5);
create table layer_l_p_polygon_6 partition of layer_l_p_polygon
    for values with (modulus 8, remainder 6);
create table layer_l_p_polygon_7 partition of layer_l_p_polygon
    for values with (modulus 8, remainder 7);

comment on table layer_l is '레이어내역';
comment on column layer_l.layer_id is '레이어ID';
comment on column layer_l.ftype is '레이어객체유형';
comment on column layer_l.fid is '객체ID';
comment on column layer_l.gmtr_val is '좌표정보';
comment on column layer_l.property is '속성정보';
comment on column layer_l.color_str is '색상문자열';
comment on column layer_l.rgst_id is '등록ID';
comment on column layer_l.rgst_dttm is '등록일시';
comment on column layer_l.mdf_id is '수정ID';
comment on column layer_l.mdf_dttm is '수정일시';