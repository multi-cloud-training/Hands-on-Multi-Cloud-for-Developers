package main

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"
)

type Car struct {
	ID     int `json:"ID"`
	UserID int `json:"UserID"`
	Model  string `json:"Model"`
}

var cars []Car

func GetCar(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	id, _ := strconv.Atoi(params["id"])

	car := getCar(id)
	if car.ID == id {
		json.NewEncoder(w).Encode(car)
		return
	}
	json.NewEncoder(w).Encode(&Car{})
}

func GetCars(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	cars := getCars()
	json.NewEncoder(w).Encode(cars)
}

func GetCarsByUser(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	id, _ := strconv.Atoi(params["id"])

	cars := getCarsForUser(id)
	json.NewEncoder(w).Encode(cars)
}

func CreateCar(w http.ResponseWriter, req *http.Request) {
	var car Car
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	_ = json.NewDecoder(req.Body).Decode(&car)
	car = createCar(car)
	json.NewEncoder(w).Encode(car)
}

func DeleteCar(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	params := mux.Vars(req)
	id, _ := strconv.Atoi(params["id"])
	deleteCar(id)
}

/*func UpdateCar(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	params := mux.Vars(r)
	for index, item := range cars {
		if item.ID == params["id"] {
			cars = append(cars[:index], cars[index+1:]...)
			var car Car
			_ = json.NewDecoder(r.Body).Decode(&car)
			car.ID = params["id"]
			cars = append(cars, car)
			json.NewEncoder(w).Encode(car)
			return
		}
	}
}*/
