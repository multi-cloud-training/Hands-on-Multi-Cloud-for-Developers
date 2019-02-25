package main

func createCar(carObj Car) Car {
	db := dbLogin()
	defer  db.Close()

	row, err := db.Query(
		"INSERT INTO cars (user_id, model) "+
		"VALUES ($1, $2) "+
		"RETURNING id",
		carObj.UserID,
		carObj.Model)

	if err != nil {
		panic (err)
	}

	row.Next()
	var newID int
	scanErr := row.Scan(&newID)

	if scanErr != nil {
		panic(scanErr)
	}

	carObj.ID = newID
	return carObj
}

func getCarsForUser(userID int) ([]Car) {
	db := dbLogin()
	defer db.Close()

	rows, err := db.Query(
		"SELECT * FROM cars WHERE user_id = $1",
		userID)

	var userCars []Car
	for rows.Next() {
		var id int
		var user_id int
		var model string

		err = rows.Scan(&id, &user_id, &model)
		if err != nil {
			panic(err)
		}

		userCars = append(userCars, Car{ID: id, Model: model, UserID: user_id})
	}
	return userCars
}

func getCar(carID int) (Car) {
	db := dbLogin()
	defer db.Close()

	rows, err := db.Query(
		"SELECT * FROM cars WHERE id = $1",
		carID)

	for rows.Next() {
		var id int
		var user_id int
		var model string

		err = rows.Scan(&id, &user_id, &model)
		if err != nil {
			panic(err)
		}
		return Car{ID: id, UserID: user_id, Model: model}
	}
	return Car{}
}

func getCars() ([]Car) {
	db := dbLogin()
	defer db.Close()

	rows, err := db.Query("SELECT * FROM cars")

	var allCars []Car
	for rows.Next() {
		var id int
		var user_id int
		var model string

		err = rows.Scan(&id, &user_id, &model)
		if err != nil {
			panic(err)
		}
		allCars = append(allCars, Car{ID: id, UserID: user_id, Model: model})
	}
	return allCars
}

func deleteCar(carID int) {
	db := dbLogin()
	defer db.Close()

	_, err := db.Query(
		"DELETE FROM cars WHERE cars.id = $1",
		carID)

	if err != nil {
		panic (err)
	}
}
