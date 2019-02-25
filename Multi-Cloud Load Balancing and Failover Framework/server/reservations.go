package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"io/ioutil"
	"github.com/gorilla/mux"
)

type Reservation struct {
	ID        int `json:"ID"`
	StartTime string `json:"StartTime"`
	EndTime   string `json:"EndTime"`
	CarID     int `json:"CarID"`
	GarageID  int `json:"GarageID"`
}

const RESERVATIONS_ROUTE string = "reservations/"

func GetReservation(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	reservation := Reservation{}
	id := params["id"]
	url := IP_ADDRESS + RESERVATIONS_ROUTE + id
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}
	error := json.Unmarshal(contents, &reservation)
	if error != nil {
		panic(error)
	}
	json.NewEncoder(w).Encode(reservation)
}

func GetReservations(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	reservations := []Reservation{}
	url := IP_ADDRESS + RESERVATIONS_ROUTE
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}
	error := json.Unmarshal(contents, &reservations)
	if error != nil {
		panic(err)
	}

	json.NewEncoder(w).Encode(reservations)
}

func GetReservationsByUser(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	reservations := []Reservation{}
	id := params["id"]
	url := IP_ADDRESS + "reservations-by-user/" + id
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}
	error := json.Unmarshal(contents, &reservations)
	if error != nil {
		panic(err)
	}

	json.NewEncoder(w).Encode(reservations)
}


func CreateReservation(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	var reservation Reservation
    _ = json.NewDecoder(req.Body).Decode(&reservation)

	url := IP_ADDRESS + RESERVATIONS_ROUTE
	jsonStr, _ := json.Marshal(reservation)
	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonStr))
	// req.Header.Set("X-Custom-Header", "myvalue")
	// req.Header.Set("Content-Type", "application/json")
	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	//body, _ := ioutil.ReadAll(resp.Body)
	_ = json.NewDecoder(resp.Body).Decode(&reservation)
	json.NewEncoder(w).Encode(reservation)
}

func DeleteReservation(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	id := params["id"]
	url := IP_ADDRESS + RESERVATIONS_ROUTE + id
	response, err := http.NewRequest("DELETE", url, nil)
	defer response.Body.Close()

	if err != nil {
		panic(err)
	}
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
