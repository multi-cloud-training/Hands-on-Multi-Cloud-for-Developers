package main

import (
	"encoding/json"
	"net/http"
	"strconv"
	"github.com/gorilla/mux"
)

type Reservation struct {
	ID        int `json:"ID"`
	StartTime string `json:"StartTime"`
	EndTime   string `json:"EndTime"`
	CarID     int `json:"CarID"`
	GarageID  int `json:"GarageID"`
}

var reservations []Reservation

func GetReservation(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	params := mux.Vars(req)
	id, _ := strconv.Atoi(params["id"])
	reservation := getReservation(id)

	if reservation.ID == id {
		json.NewEncoder(w).Encode(reservation)
		return
	}

	json.NewEncoder(w).Encode(&Reservation{})
}

func GetReservations(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	reservations := getReservations()
	json.NewEncoder(w).Encode(reservations)
}

func GetReservationsByUser(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	params := mux.Vars(req)
	id, _ := strconv.Atoi(params["id"])
	reservations := getReservationsForUser(id)
	json.NewEncoder(w).Encode(reservations)
}


func CreateReservation(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	var reservation Reservation
	_ = json.NewDecoder(req.Body).Decode(&reservation)
	reservation = createReservation(reservation)
	json.NewEncoder(w).Encode(reservation)
}

func DeleteReservation(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	params := mux.Vars(req)
	id, _ := strconv.Atoi(params["id"])
	deleteReservation(id)
}

/*func UpdateReservation(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	params := mux.Vars(r)
	for index, item := range reservations {
		if item.ID == params["id"] {
			reservations = append(reservations[:index], reservations[index+1:]...)
			var reservation Reservation
			_ = json.NewDecoder(r.Body).Decode(&reservation)
			reservation.ID = params["id"]
			reservations = append(reservations, reservation)
			json.NewEncoder(w).Encode(reservation)
			return
		}
	}
}*/
