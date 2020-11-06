create table food
(
    id   bigint auto_increment,
    name varchar(100) not null,
    primary key (id)
);

create table pet
(
    id      bigint auto_increment,
    name    varchar(100) not null,
    food_id bigint       not null,
    primary key (id),
    foreign key (food_id) references food (id),
    unique (name)
);
