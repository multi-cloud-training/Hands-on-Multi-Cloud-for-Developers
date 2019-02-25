package main

import "time"

func createReservation(reservationObj Reservation) Reservation {
	db := dbLogin()
	defer db.Close()

	time_layout := "2006-01-02T15:04:05.000Z"
	start, _ := time.Parse(time_layout, reservationObj.StartTime)
	end, _ := time.Parse(time_layout, reservationObj.EndTime)
	row, err := db.Query(
		"INSERT INTO reservations (start_time, end_time, car_id, garage_id) "+
		"VALUES($1, $2, $3, $4) "+
		"RETURNING id",
		start,
		end,
		reservationObj.CarID,
		reservationObj.GarageID)

	if err != nil {
		panic(err)
	}

	row.Next()
	var newID int
	scanErr := row.Scan(&newID)

	if scanErr != nil {
		panic(scanErr)
	}

	reservationObj.ID = newID
	return reservationObj
}

func getReservationsForUser(userID int) []Reservation {
	db := dbLogin()
	defer db.Close()

	rows, err := db.Query(
		"SELECT r.id, r.start_time, r.end_time, r.car_id, r.garage_id "+
		"FROM reservations r "+
		"INNER JOIN cars on r.car_id = cars.id "+
		"INNER JOIN users on users.id = cars.user_id "+
		"WHERE users.id = $1",
		userID)
	var reservations []Reservation

	for rows.Next() {
		var id int
		var start string
		var end string
		var carID int
		var garageID int

		err = rows.Scan(&id, &start, &end, &carID, &garageID)
		if err != nil {
			panic(err)
		}

		reservations = append(reservations,
			Reservation{ID: id, StartTime: start, EndTime: end, CarID: carID, GarageID: garageID})
	}
	return reservations

}

func getReservationsForGarage(garageID int) []Reservation {
	db := dbLogin()
	defer db.Close()

	rows, err := db.Query(
		"SELECT * FROM reservations WHERE garage_id = $1",
		garageID)

	var reservations []Reservation

	for rows.Next() {
		var id int
		var start string
		var end string
		var carID int
		var garageID int

		err = rows.Scan(&id, &start, &end, &carID, &garageID)
		if err != nil {
			panic(err)
		}

		reservations = append(reservations,
			Reservation{ID: id, StartTime: start, EndTime: end, CarID: carID, GarageID: garageID})
	}
	return reservations

}

func getReservationForCar(carID int) (Reservation) {
	db := dbLogin()
	defer db.Close()

	rows, err :=  db.Query(
		"SELECT * FROM reservations WHERE car_id = $1",
		carID)

	for rows.Next() {
		var id int
		var start string
		var end string
		var carID int
		var garageID int

		err = rows.Scan(&id, &start, &end, &carID, &garageID)
		if err != nil {
			panic(err)
		}

		return Reservation{ID: id, StartTime: start, EndTime: end, CarID: carID, GarageID: garageID}
	}
	return Reservation{}
}

func getReservations() ([]Reservation) {
	db := dbLogin()
	defer db.Close()

	rows, err := db.Query("SELECT * FROM reservations")

	var reservations []Reservation
	for rows.Next() {
		var id int
		var start string
		var end string
		var carID int
		var garageID int

		err = rows.Scan(&id, &start, &end, &carID, &garageID)
		if err != nil {
			panic(err)
		}

		reservations = append(reservations,
			Reservation{ID: id, StartTime: start, EndTime: end, CarID: carID, GarageID: garageID})
	}

	return reservations
}

func getReservation(reservationID int) (Reservation) {
	db := dbLogin()
	defer db.Close()

	rows, err := db.Query(
		"SELECT * FROM reservations WHERE id = $1",
		reservationID)

	for rows.Next() {
		var id int
		var start string
		var end string
		var carID int
		var garageID int

		err = rows.Scan(&id, &start, &end, &carID, &garageID)
		if err != nil {
			panic(err)
		}

		return Reservation{ID: id, StartTime: start, EndTime: end, CarID: carID, GarageID: garageID}
	}
	return Reservation{}
}

func deleteReservation(reservationID int) {
	db := dbLogin()
	defer db.Close()

	_, err := db.Query(
		"DELETE FROM reservations WHERE reservations.id = $1",
		reservationID)

	if err != nil {
		panic(err)
	}
}
