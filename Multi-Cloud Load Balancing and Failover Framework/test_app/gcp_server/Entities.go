package main

type User struct {
	Id int
	Username string
	Password string
	Email string
}

type Car struct {
	Model string
	Id int
	UserId int
}

type Reservation struct {
	Id int
	StartTime string
	EndTime string
	CarId int
	GarageId int
}

type Garage struct {
	Name string
	MaxCars int
	Id int
}
