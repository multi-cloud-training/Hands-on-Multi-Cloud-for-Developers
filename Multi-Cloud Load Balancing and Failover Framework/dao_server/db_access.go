package main

import (
  "database/sql"
  "fmt"
  "os"
  "strconv"
  _ "github.com/lib/pq"
)

type DatabaseInfo struct {
	Host     string
	User     string
	Password string
	Name     string
	Port     int
}

/*func main() {
	testUser := getUser("jtrinh", "ec528")
	fmt.Println(testUser.Username, testUser.Email)
	userCars := getCarsForUser(testUser.ID)

	for _, car := range userCars {
		fmt.Println(car.Model)
		reservation := getReservationForCar(car.ID)
		fmt.Println(reservation.StartTime, reservation.EndTime)
	}

	garages := getGarages()

	for _, garage := range garages {
		fmt.Println(garage.Name)
		reservations := getReservationsForGarage(garage.ID)
		for _, reservation := range reservations {
			fmt.Println(reservation.StartTime, reservation.EndTime)
		}
	}

	reservationsForUser := getReservationsForUser(testUser.ID)
	for _, reservation := range reservationsForUser {
		fmt.Println(reservation.StartTime, reservation.EndTime)
	}

	newUser := User{Username: "TestUser", Password: "lolhashed", Email: "pls@bu.edu"}
	newUser = createUser(newUser)

	fmt.Println("User ID: ", newUser.ID)

	newCar := Car{Model: "R8", UserID: newUser.ID}
	newCar = createCar(newCar)

	newGarage := Garage{Name: "Decent", MaxCars: 7}
	newGarage = createGarage(newGarage)

	newReservation := Reservation {StartTime: time.Now().String(), EndTime: time.Now().String(), CarID: newCar.ID,GarageID: newGarage.ID}
	newReservation = createReservation(newReservation)

	deleteReservation(newReservation.ID)
	deleteGarage(newGarage.ID)
	deleteCar(newCar.ID)
	deleteUser(newUser.ID)
}*/

func testSelect() {
	db := dbLogin()
	defer db.Close()
	rows, err := db.Query("SELECT * FROM users")
	if err != nil {
		panic(err)
	}

	for rows.Next() {
		var id int
		var username string
		var password string
		var email string

		err = rows.Scan(&id, &username, &password, &email)
		if err != nil {
			panic(err)
		}

		fmt.Println(id, username, password, email)
	}
}

func dbLogin() (*sql.DB) {
	port, _ := strconv.Atoi(os.Getenv("PORT"))
	psqlInfo := fmt.Sprintf(
		"host=%s port=%d user=%s password=%s dbname=%s sslmode=disable",
		os.Getenv("HOST"),
		port,
		os.Getenv("USER"),
		os.Getenv("PASSWORD"),
		os.Getenv("NAME"))
	db, err := sql.Open("postgres", psqlInfo)

	if err != nil {
		panic(err)
	}

	err = db.Ping()
	if err != nil {
		panic(err)
	}

	return db
}
