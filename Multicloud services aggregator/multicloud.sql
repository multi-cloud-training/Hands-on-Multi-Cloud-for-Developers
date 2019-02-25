drop database if exists multicloud;
create database multicloud;
use multicloud;

create table csp
(
  csp_id int not null auto_increment,
  csp_email_id varchar(255)  not null,
  csp_name varchar(255) not null,
  csp_password varchar(255) not null,
  csp_join_date date not null,
  csp_bank_account_number bigint not null,
  primary key (csp_id)
);

create table order_
(
  order_id int not null auto_increment,
  order_date date not null,
  number_of_machines int not null,
  # instance_type varchar(255) not null,
  ca_id int not null,
  customer_id int not null,
  cpu_cores int,
  ram int,
  disk_size varchar(20) not null,
  order_end_date date,
  order_amount int not null,
  order_cost int not null,
  primary key (order_id)
);

create table ca
(
  ca_id int not null auto_increment,
  ca_email_id varchar(255) not null,
  ca_name char(255) not null,
  ca_bank_account_number bigint not null,
  ca_password varchar(255) not null,
  primary key(ca_id)
);

create table customer
(
  customer_id int not null auto_increment,
  customer_email_id varchar(255) not null,
  customer_name char(255) not null,
  customer_password varchar(255) not null,
  customer_join_date date not null,
  customer_bank_account bigint(16) not null,
  customer_offer_id int,
  customer_isDelete boolean default false,
  primary key(customer_id)
);

create table bill
(
  bill_id int not null auto_increment,
  bill_amount int(12) not null,
  csp_id int,
  ca_id int not null,
  customer_id int,
  month int not null,
  year int not null,
  is_paid bool default False,
  primary key(bill_id)
);

create table offer
(
  offer_id int not null auto_increment,
  offer_name varchar(255) not null,
  discount int not null,
  ca_id int not null,
  valid_till date,
  is_used bool default False,
  primary key(offer_id)
);

create table machine
(
  mac_id int not null auto_increment,
  csp_id int not null,
  # gpu varchar(20) not null,
  disk_size varchar(20) not null,
  ram int(4) not null,
  cpu_cores int(4) not null,
  # os char(20) not null,
  ip_address varchar(16) not null,
  price int not null,
  order_id int,
  primary key(mac_id, csp_id)
);

create table receives
(
  csp_id int not null,
  order_id int not null,
  quantity int not null,
  csp_cost int not null,
  primary key (csp_id,order_id)
);

create table onboards
(
  ca_id int not null,
  customer_id int not null,
  primary key (ca_id, customer_id)
);

# create table avails
  # (
      # offer_id int not null,
      # customer_id int not null,
      # from_date date not null,
      # primary key(offer_id, customer_id)
  # );

# create table attached
  # (
      # bill_id int not null,
      # offer_id int not null,
      # primary key(bill_id, offer_id)
  # );

create table csp_contracts
(
  ca_id int not null,
  csp_id int not null,
  primary key(ca_id,csp_id)
);

create view order_customer as select order_id, order_date, number_of_machines, ca_id, customer_id, cpu_cores, ram, disk_size, order_end_date, order_amount from order_;
create view order_csp as select ord.order_id, ord.order_date, ord.number_of_machines, ord.ca_id, r.csp_id, ord.cpu_cores, ord.ram, ord.disk_size, ord.order_end_date, ord.order_cost
from order_ ord, receives r where ord.order_id=r.order_id;
create view machine_customer as select mac_id, disk_size, ram, cpu_cores, ip_address, order_id from machine;

create view customer_bill as select bill_id, customer_id, ca_id, month, year, bill_amount, is_paid from bill where csp_id is null;
create view ca_bill as select bill_id, ca_id, csp_id, month, year, bill_amount, is_paid from bill where customer_id is null;

alter table order_ add constraint fk_order_ca_id foreign key (ca_id) references ca(ca_id) ;
alter table order_ add constraint fk_order_customer_id foreign key (customer_id) references customer(customer_id);


alter table bill add constraint fk_bill_csp_id foreign key (csp_id) references csp(csp_id);
alter table bill add constraint fk_bill_ca_id foreign key (ca_id) references ca(ca_id);
alter table bill add constraint fk_bill_cust_id foreign key (customer_id ) references customer(customer_id);
alter table machine add constraint fk_machine_csp_id foreign key (csp_id) references csp(csp_id);
alter table machine add constraint fk_machine_order_id foreign key (order_id) references order_(order_id);
alter table receives add constraint fk_receives_csp_id foreign key (csp_id) references csp(csp_id);
alter table receives add constraint fk_receives_order_id foreign key (order_id) references order_(order_id);
alter table onboards add constraint fk_onboards_ca_id foreign key (ca_id) references ca(ca_id);
alter table onboards add constraint fk_onboards_customer_id foreign key (customer_id) references customer(customer_id);
alter table customer add constraint fk_customer_offer_id foreign key (customer_offer_id)  references offer(offer_id) on delete set null;
alter table offer add constraint fk_offer_ca_id foreign key (ca_id) references ca(ca_id);
alter table csp_contracts add constraint fk_csp_contracts_csp_id foreign key (csp_id) references csp(csp_id);
alter table csp_contracts add constraint fk_csp_contracts_ca_id foreign key (ca_id) references ca(ca_id);


###### Ca
insert into ca values(12121,'abah@gmail.com','khas', 132121, 'pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e');
insert into ca values(232323,'sds@gmail.com','dsds', 12434121, 'pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e');
insert into ca values(4324323,'fdfds@gmail.com','hgh',5454545, 'pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e');
insert into ca values (123,'multicloud@gmail.com','multicloud',1361,'pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e');


###### Customer
insert into customer values (11224,'Rohit@gmail.com','Rohit','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-09-09',3434,null, False);
insert into customer values (11225,'Li@gmail.com','Li','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-09-19',3434,null, False);
insert into customer values (11226,'Rakesh@gmail.com','Rakesh','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-09-29',3434,null, False);
insert into customer values (11227,'Laxmi@gmail.com','Laxmi','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-01-09',3434,null, False);
insert into customer values (11228,'Ravi@gmail.com','Ravi', 'pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-02-09',3434,null, False);
insert into customer values (11229,'John@gmail.com','John','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-03-09',3434,null, False);
insert into customer values (11220,'Wayne@gmail.com','Wayne','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-04-09',3434,null, False);
insert into customer values (11241,'Kaka@gmail.com','Kaka','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-09-05',3434,null, False);
#insert into customer values (11242,'maulik@gmail.com','maulik','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-09-05',3434,null, False);


###### CSP
insert into csp values (1234,'amazon@gmail.com','AWS','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-09-09',3434);
insert into csp values (1235,'google@gmail.com','Google','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-08-01',3435);
insert into csp values (1236,'microsoft@gmail.com','Azure','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-10-10',34346);
insert into csp values (12361,'VMwaret@gmail.com','vCloudAir','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-10-01',34347);
insert into csp values (12362,'Rackspace@gmail.com','RackConnect','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-11-10',34348);
insert into csp values (12363,'HPE@gmail.com','Right Mix','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-10-10',34349);
insert into csp values (12364,'EMC@gmail.com','VCE','pbkdf2:sha256:50000$PJ8gdds4$21c76a7ebbe9fd90740db011db11d1945c9806ff5b312a49ee362f9cc423416e','2000-12-10',343461);

##### Machines
#AWS Machines
insert into machine values(1334151,1234,'1TB',4,2,'123.65.254.22',20,null);
insert into machine values(1334152,1234,'1TB',4,2,'123.65.251.22',145,null);
insert into machine values(1334153,1234,'1TB',4,2,'123.65.254.32',100,null);
insert into machine values(1334154,1234,'1TB',4,8,'123.65.254.52',250,null);
insert into machine values(1334155,1234,'2TB',8,8,'123.65.251.12',450,null);
insert into machine values(1334156,1234,'2TB',8,4,'123.65.252.12',4500,null);
insert into machine values(1334157,1234,'2TB',8,4,'123.65.253.12',450000,null);

insert into machine values(1334158,1234,'4TB',4,8,'123.65.254.53',150,null);
insert into machine values(1334159,1234,'6TB',8,8,'123.65.251.12',420,null);
insert into machine values(1334160,1234,'8TB',8,4,'123.65.252.12',3500,null);
insert into machine values(1334161,1234,'2TB',8,4,'123.65.253.12',450000,null);

#Google
insert into machine values(1234151,1235,'1TB',4,4,'123.65.254.22',545,null);
insert into machine values(1134151,1235,'1TB',4,4,'123.65.254.32',145,null);
insert into machine values(13134151,1235,'2TB',4,4,'123.65.254.42',2345,null);
insert into machine values(134151,1235,'2TB',8,2,'123.65.254.52',450,null);
insert into machine values(1354151,1235,'2TB',8,2,'123.65.254.62',45000,null);

insert into machine values(131341551,1235,'2TB',4,4,'123.65.254.42',2345,null);
insert into machine values(13415154,1235,'6TB',8,2,'123.65.254.52',450,null);
insert into machine values(13541571,1235,'8TB',8,2,'123.65.254.62',45000,null);


#vCloud
insert into machine values(1114151,12361,'1TB',4,4,'121.65.254.12',100,null);
insert into machine values(1214151,12361,'1TB',8,2,'122.65.254.12',150,null);
insert into machine values(1314151,12361,'2TB',8,4,'124.65.254.12',250,null);

#Azure
insert into machine values(1334111,1236,'1TB',8,2,'123.65.254.11',145,null);
insert into machine values(1334121,1236,'1TB',8,4,'123.65.254.13',245,null);
insert into machine values(1334131,1236,'2TB',8,2,'123.65.254.12',415,null);
insert into machine values(1334141,1236,'2TB',4,2,'123.65.254.14',450,null);
insert into machine values(1334101,1236,'2TB',4,4,'123.65.254.15',4500,null);

#RackConnect
insert into machine values(1334111,12362,'1TB',8,4,'113.25.254.12',450,null);
insert into machine values(1334112,12362,'1TB',8,4,'113.26.254.12',100,null);
insert into machine values(1334113,12362,'2TB',4,4,'113.35.254.12',800,null);

#Right Mix
insert into machine values(1034151,12363,'1TB',4,4,'120.65.254.12',415,null);
insert into machine values(1934151,12363,'1TB',8,2,'129.65.254.12',425,null);
insert into machine values(1834151,12363,'2TB',4,4,'183.65.254.12',435,null);

#VCE
insert into machine values(1534151,12364,'1TB',8,2,'123.85.254.12',4500,null);
insert into machine values(1634151,12364,'2TB',4,4,'123.75.254.12',4590,null);
insert into machine values(1734151,12364,'2TB',8,4,'123.55.254.12',4580,null);


###### CSP_Contracts
insert into csp_contracts values (123,1234);
insert into csp_contracts values (123,1235);
insert into csp_contracts values (123,1236);
insert into csp_contracts values(12121,1234);
insert into csp_contracts values(12121,1235);
insert into csp_contracts values(12121,12361);
insert into csp_contracts values(12121,12364);

insert into csp_contracts values(232323,1234);
insert into csp_contracts values(232323,1235);
insert into csp_contracts values(232323,1236);
insert into csp_contracts values(232323,12364);

insert into csp_contracts values(4324323,1234);
insert into csp_contracts values(4324323,1235);
insert into csp_contracts values(4324323,1236);
insert into csp_contracts values(4324323,12361);
insert into csp_contracts values(4324323,12362);
insert into csp_contracts values(123,12363);
insert into csp_contracts values(4324323,12364);




###### Onboards
insert into onboards values (12121,11224);
insert into onboards values (12121,11225);
insert into onboards values (12121,11220);
insert into onboards values (12121,11241);

insert into onboards values (232323,11224);
insert into onboards values (232323,11225);
insert into onboards values (232323,11226);
insert into onboards values (232323,11227);
insert into onboards values (232323,11228);
insert into onboards values (232323,11229);
insert into onboards values (232323,11220);
insert into onboards values (232323,11241);

insert into onboards values (4324323,11224);
insert into onboards values (4324323,11225);
insert into onboards values (4324323,11226);
insert into onboards values (4324323,11227);
insert into onboards values (4324323,11228);
insert into onboards values (4324323,11229);
insert into onboards values (4324323,11220);
insert into onboards values (4324323,11241);


##### Offer
insert into offer values (4321,'Big Bang Offer',9,12121, null, False);
insert into offer values (4322,'Bumpper Offer',11,232323, null, False);
insert into offer values (4323,'Super Deal Offer',5,232323, null, False);
insert into offer values (4324,'Platinum Offer',20,4324323, null, False);
insert into offer values (4325,'Gold Bang Offer',15,4324323, null, False);
insert into offer values (4326,'Welcome Offer',10,123, null, False);

##### Bill
insert into bill values (0001,5000,1234,123,11224,'01','2000', False);
insert into bill values (0002,1000,12361,123,11227,'01','2000', False);
insert into bill values (0003,2000,12364,123,11220,'01','2000', False);


insert into bill values (1004,3000,1235,232323,11225,'02','2000', False);
insert into bill values (1005,53000,12362,232323,11228,'03','2000', False);
insert into bill values (1006,51000,1236,232323,11241,'01','2000', False);

insert into bill values (2004,4000,1236,4324323,11226,'07','2000', False);
insert into bill values (2005,43000,12364,4324323,11229,'08','2000', False);
insert into bill values (2006,41000,1235,4324323,11241,'09','2000', False);

##### Order
insert into order_ values(0010,'2000-02-01',5,12121,11224,16,16,"1TB",'2000-10-10', 40, 30);
insert into order_ values(0011,'2000-03-01',10,4324323,11227,16,16,"1TB",'2000-10-10', 50, 40);
insert into order_ values(0012,'2000-04-01',4,232323,11226,16,16,"2TB",'2000-10-10', 40, 30);

insert into order_ values(1010,'2000-11-01',5,232323,11225,32,16,"2TB",'2001-10-10', 60, 30);
insert into order_ values(1011,'2000-12-01',5,12121,11228,4,16,"1TB",'2001-10-10', 70, 30);
insert into order_ values(1012,'2000-02-01',5,4324323,11229,8,16,"2TB",'2000-10-10', 50, 40);

insert into order_ values(2011,'2000-01-01',5,12121,11220,8,16,"2TB",'2000-10-10', 70, 60);
insert into order_ values(2012,'2000-11-01',15,4324323,11241,8,16,"1TB",'2001-10-10', 70, 30);
insert into order_ values(2013,'2000-01-01',5,4324323,11229,8,16,"2TB",null, 25, 20);


##### Receives
insert into receives values(1234,0010,5,30);
insert into receives values(12361,0011,10,40);
insert into receives values(1236,0012,4,50);
insert into receives values(1235,1010,5,40);
insert into receives values(12362,1011,5,40);
insert into receives values(12364,1012,5,30);
insert into receives values(12364,2011,5,50);
insert into receives values(1235,2012,15,30);
insert into receives values(12364,2013,5,50);

delimiter $$
create definer=`root`@`localhost` procedure `sp_create_customer`(
in sp_email_id varchar(255),
in sp_name varchar(255),
in sp_password varchar(255),
in sp_bank_account_number bigint,
in sp_ca_id int
)
begin

	declare temp_custId int default 0;
	declare exit handler for sqlexception
    begin
		select 'Error occured';
        rollback;
        resignal;
	end;
	start transaction;
		if ( select exists (select 1 from customer where customer_email_id = sp_email_id) ) then

			select 'Customer exists !!';

		else

			insert into customer (customer_email_id, customer_name, customer_password, customer_join_date, customer_bank_account, customer_offer_id) values (sp_email_id, sp_name, sp_password, CURDATE(), sp_bank_account_number, null);
			#select customer_id into temp_custId from customer where customer_email_id=sp_email_id;
			insert into onboards (ca_id, customer_id) values (sp_ca_id, (select customer_id from customer where customer_email_id=sp_email_id));

end if;
end$$
delimiter ;

delimiter $$
create definer=`root`@`localhost` procedure `sp_create_csp`(
in c_email_id varchar(200),
in c_name varchar(200),
in c_password varchar(200),
in c_bank_account_number bigint,
in c_ca_id int
)
begin
	declare exit handler for sqlexception
    begin
		select 'Error occured';
        rollback;
        resignal;
	end;
	start transaction;
	if ( select exists (select 1 from csp where csp_email_id = c_email_id) ) then

		select 'CSP exists !!';

	else

		insert into csp ( csp_email_id, csp_name, csp_password, csp_join_date, csp_bank_account_number) values (c_email_id, c_name, c_password, CURDATE(), c_bank_account_number);
		insert into csp_contracts (ca_id, csp_id) values (c_ca_id, (select csp_id from csp where csp_email_id=c_email_id));

end if;
end$$
delimiter ;

delimiter $$
create definer=`root`@`localhost` procedure `sp_create_ca`(
in ca_email_id varchar(200),
in ca_name varchar(200),
in ca_password varchar(200),
in ca_bank_account_number bigint
)
begin

	insert into ca ( ca_email_id, ca_name, ca_password, ca_bank_account_number) values (ca_email_id, ca_name, ca_password, ca_bank_account_number);

end$$
delimiter ;

delimiter $$
create definer=`root`@`localhost` procedure `sp_create_order`(
in sp_email_id varchar(255),
in sp_ram int,
in sp_cpu int,
in sp_disk_size varchar(255),
in sp_no_of_machines int,
in sp_customer_id int,
in sp_ca_id int
)
begin

	declare temp_count int;
	declare temp_price int;
	declare temp_last_order_id int;

	declare exit handler for sqlexception
    begin
		select 'Error occured';
        rollback;
        resignal;
	end;
	start transaction;

		select count(s.mac_id), sum(s.price) into temp_count, temp_price  from (select m.* from csp_contracts c join machine m on c.csp_id=m.csp_id where c.ca_id=sp_ca_id and m.cpu_cores=sp_cpu and m.ram=sp_ram and m.disk_size=sp_disk_size and m.order_id is null order by m.price limit sp_no_of_machines) as s;

		if ( temp_count = sp_no_of_machines ) then

		insert into order_ (order_date, number_of_machines, ca_id, customer_id, cpu_cores, ram, disk_size, order_end_date, order_amount, order_cost) values (CURDATE(), sp_no_of_machines,sp_ca_id,sp_customer_id, sp_cpu, sp_ram, sp_disk_size, null, temp_price*1.2, temp_price);
		#select m.mac_id, m.csp_id, m.price from csp_contracts c join machine m on c.csp_id=m.csp_id where c.ca_id=sp_ca_id and m.cpu_cores=sp_cpu and m.ram=sp_ram and m.disk_size=sp_disk_size order by m.price limit 1;

		set temp_last_order_id = LAST_INSERT_ID();

        update  machine m1 join (select m.mac_id from csp_contracts c join machine m on c.csp_id=m.csp_id where c.ca_id=sp_ca_id and m.cpu_cores=sp_cpu and m.ram=sp_ram and m.disk_size=sp_disk_size and m.order_id is null order by m.price limit sp_no_of_machines) s
		on m1.mac_id=s.mac_id set m1.order_id=temp_last_order_id;

		insert into receives (csp_id, order_id, csp_cost, quantity) select m.csp_id, m.order_id, sum(m.price), count(m.mac_id) from machine m where m.order_id=temp_last_order_id group by m.csp_id;

		else
		select 'Not enough resources available!!';
		end if;

end$$
delimiter ;

delimiter $$
use multicloud $$
create definer=`root`@`localhost` procedure `sp_generate_bill_csp`(
in sp_day int,
in sp_month int,
in sp_year int,
in sp_csp_id int,
in sp_ca_id int
)
begin

declare order_month int;
declare order_start_day int;
declare order_end_day int;
declare order_cost int;
declare total_monthly_bill int;
declare finished int default 0;
declare ca_order_cursor cursor for select month(o.order_date) as order_month, day(o.order_date) as order_start_day, day(o.order_end_date) as order_end_day, r.csp_cost as order_cost
from order_ as o join receives as r on o.order_id = r.order_id and r.csp_id = sp_csp_id and o.ca_id = sp_ca_id and ( (o.order_end_date is null) or (month(o.order_end_date) = sp_month and year(o.order_end_date) = sp_year));
declare continue handler for not found set finished = 1;

declare exit handler for sqlexception
    begin
		select 'Error occured';
        rollback;
        resignal;
	end;
set total_monthly_bill = 0;

start transaction;

open ca_order_cursor;

get_ca_order: LOOP
 FETCH ca_order_cursor INTO order_month, order_start_day, order_end_day, order_cost;
 IF finished = 1 THEN
  LEAVE get_ca_order;
 END IF;
 -- compute cost
 IF order_month < sp_month THEN
  IF order_end_day is null THEN
   set total_monthly_bill = total_monthly_bill + (30 * order_cost);
  ELSE
   set total_monthly_bill = total_monthly_bill + (order_end_day * order_cost);
  END IF;
 ELSEIF order_month = sp_month THEN
  IF order_end_day is null THEN
   set total_monthly_bill = total_monthly_bill + ( (30 - order_start_day + 1) * order_cost);
  ELSE
   set total_monthly_bill = total_monthly_bill + ( (order_end_day - order_start_day + 1) * order_cost);
  End IF;
 END IF;
END LOOP get_ca_order;

close ca_order_cursor;

insert into bill (bill_amount, csp_id, ca_id, customer_id, month, year, is_paid) values (total_monthly_bill, sp_csp_id, sp_ca_id, null, sp_month, sp_year, False);

select concat("New bill with cost: ", total_monthly_bill," generated for ca: ", sp_ca_id, " by csp: ", sp_csp_id, " for month: ", sp_month, " year: ", sp_year);

commit;

end$$
delimiter ;

delimiter $$
use multicloud $$
create definer=`root`@`localhost` procedure `sp_generate_bill_ca`(
in sp_day int,
in sp_month int,
in sp_year int,
in sp_ca_id int,
in sp_customer_id int
)
begin

declare order_month int;
declare order_start_day int;
declare order_end_day int;
declare order_amount int;
declare total_monthly_bill int;
declare id int;
declare discount int;
declare offer_discount int default 0;
declare offer_id int default null;
declare finished int default 0;
declare customer_order_cursor cursor for select month(o.order_date) as order_month, day(o.order_date) as order_start_day, day(o.order_end_date) as order_end_day, o.order_amount as order_amount
from order_ as o join customer as c on o.customer_id = c.customer_id and o.customer_id = sp_customer_id and o.ca_id = sp_ca_id and (o.order_end_date is null or (month(o.order_end_date) = sp_month and year(o.order_end_date) = sp_year));
declare customer_offer_cursor cursor for select o.offer_id, o.discount
from offer as o join customer as c on o.offer_id = c.customer_offer_id and o.ca_id = sp_ca_id and o.is_used is False and (sp_month < month(o.valid_till) or (month(o.valid_till) = sp_month and 30 <= day(o.valid_till))) and year(o.valid_till) <= sp_year;
declare continue handler for not found set finished = 1;

declare exit handler for sqlexception
    begin
		select 'Error occured';
        rollback;
        resignal;
	end;

start transaction;
set total_monthly_bill = 0;

open customer_order_cursor;

get_customer_order: LOOP
 FETCH customer_order_cursor INTO order_month, order_start_day, order_end_day, order_amount;
 IF finished = 1 THEN
  LEAVE get_customer_order;
 END IF;
 -- compute cost
 IF order_month < sp_month THEN
  IF order_end_day is null THEN
   set total_monthly_bill = total_monthly_bill + (30 * order_amount);
  ELSE
   set total_monthly_bill = total_monthly_bill + (order_end_day * order_amount);
  END IF;
 ELSEIF order_month = sp_month THEN
  IF order_end_day is null THEN
   set total_monthly_bill = total_monthly_bill + ( (30 - order_start_day + 1) * order_amount);
  ELSE
   set total_monthly_bill = total_monthly_bill + ( (order_end_day - order_start_day + 1) * order_amount);
  End IF;
 END IF;
END LOOP get_customer_order;

close customer_order_cursor;

open customer_offer_cursor;

get_customer_offer: LOOP
 FETCH customer_offer_cursor INTO id, discount;
 IF finished = 1 THEN
  LEAVE get_customer_offer;
 END IF;
 -- find max offer
 IF offer_discount < discount THEN
  set offer_discount = discount;
  set offer_id = id;
 END IF;
END LOOP get_customer_offer;

close customer_offer_cursor;

IF (offer_id is not null) and (offer_discount != 0) THEN
 set total_monthly_bill = convert(total_monthly_bill * ((100 - offer_discount)/100),unsigned int);
 update offer as o set o.is_used = True where o.offer_id = offer_id and o.discount = offer_discount;
END IF;

insert into bill (bill_amount, csp_id, ca_id, customer_id, month, year, is_paid, offer_id) values (total_monthly_bill, null, sp_ca_id, sp_customer_id, sp_month, sp_year, False, offer_id);

select concat("New bill with cost: ", total_monthly_bill, " with discount: ", offer_discount," generated for customer: ", sp_customer_id, " by ca: ", sp_ca_id, " for month: ", sp_month, " year: ", sp_year);
commit;

end$$
delimiter ;

###### Stored Procedure to update CA delimiter
delimiter $$
use multicloud $$
create definer=`root`@`localhost` procedure `sp_update_ca`(
    in sp_id int,
    in sp_email_id varchar(255) ,
    in sp_name varchar(255) ,
    in sp_password varchar(255),
    in sp_bank_account_number bigint
)
begin
    if (select exists (select 1 from ca where ca_id = sp_id)) then
		update ca set ca_name = sp_name, ca_email_id = sp_email_id, ca_password = sp_password, ca_bank_account_number = sp_bank_account_number where ca_id=sp_id;
    else
        select 'Not enough resources available!!';
    end if;
end$$
delimiter ;

###### Stored Procedure to update CSP delimiter $$
delimiter $$
use multicloud $$
create definer=`root`@`localhost` procedure `sp_update_csp`(
    in sp_id int,
    in sp_email_id varchar(255) ,
    in sp_name varchar(255) ,
    in sp_password varchar(255),
    in sp_bank_account_number bigint
)
begin
    if (select exists (select 1 from csp where csp_id = sp_id)) then
        update csp set csp_name = sp_name, csp_email_id = sp_email_id, csp_password = sp_password, csp_bank_account_number = sp_bank_account_number where csp_id = sp_id;
    else
        select 'Not enough resources available!!';
    end if;
end$$ delimiter ;

###### Stored Procedure to update customer delimiter $$
delimiter $$
use multicloud $$
create definer=`root`@`localhost` procedure `sp_update_customer`(
    in sp_id int,
    in sp_email_id varchar(255) ,
    in sp_name varchar(255) ,
    in sp_password varchar(255),
    in sp_bank_account_number bigint)
begin
    if (select exists (select 1 from customer where customer_id = sp_id)) then
        update customer set customer_name = sp_name, customer_email_id = sp_email_id, customer_password = sp_password, customer_bank_account = sp_bank_account_number where customer_id = sp_id;
	else
        select 'Not enough resources available!!';
    end if;
end$$ delimiter ;

# Update order_id in machines after deleting customers
DROP TRIGGER IF EXISTS `multicloud`.`customer_AFTER_UPDATE`;
DELIMITER $$
CREATE DEFINER = CURRENT_USER TRIGGER `multicloud`.`customer_AFTER_UPDATE` AFTER UPDATE ON `customer` FOR EACH ROW
BEGIN
update order_
set order_end_date = curdate()
where customer_id = new.customer_id;

SET SQL_SAFE_UPDATES = 0;

update order_,machine
set machine.order_id = null
where machine.order_id = order_.order_id and 
customer_id = new.customer_id;

 SET SQL_SAFE_UPDATES = 1;

END$$
DELIMITER ;

# Add Welcome Offer Trigger
DELIMITER $$
DROP TRIGGER IF EXISTS `multicloud`.`customer_AFTER_INSERT` $$
DELIMITER ;
DROP TRIGGER IF EXISTS `multicloud`.`customer_BEFORE_INSERT`;

DELIMITER $$
USE `multicloud`$$
CREATE DEFINER = CURRENT_USER TRIGGER `multicloud`.`customer_BEFORE_INSERT` BEFORE INSERT ON `customer` FOR EACH ROW
BEGIN
SET new.customer_offer_id = '4326';
END$$
DELIMITER ;

###### Stored Procedure to update customer delimiter $$
delimiter $$
use multicloud $$
create definer=`root`@`localhost` procedure `sp_update_customer_admin`(
    in sp_id int,
    in sp_email_id varchar(255) ,
    in sp_name varchar(255) ,
    in sp_bank_account_number bigint,
 	in sp_offer_id int )
begin
    if (select exists (select 1 from customer where customer_id = sp_id)) then
        update customer set customer_name = sp_name, customer_email_id = sp_email_id, customer_bank_account = sp_bank_account_number, customer_offer_id=sp_offer_id where customer_id = sp_id;
	else
        select 'Not enough resources available!!';
    end if;
end$$ delimiter ;

###### Stored Procedure to update customer delimiter $$
delimiter $$
use multicloud $$
create definer=`root`@`localhost` procedure `sp_end_order`(
    in sp_order_id int,
    in sp_order_id_2 int)
begin
    if (select exists (select 1 from order_ where order_id = sp_order_id)) then
        update order_ set order_end_date = curdate() where order_id = sp_order_id;
        update machine set order_id=null where order_id = sp_order_id;
	else
        select 'Not enough resources available!!';
    end if;
end$$ delimiter ;
