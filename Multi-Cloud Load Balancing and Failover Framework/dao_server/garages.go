package main

import (
	"encoding/json"
	"net/http"
	"strconv"
	"github.com/gorilla/mux"
)

type Garage struct {
	ID      int `json:"ID"`
	Name    string `json:"Name"`
	MaxCars int `json:"MaxCars"`
}

var garages []Garage

func GetGarage(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	id, _ := strconv.Atoi(params["id"])
	garage := getGarage(id)

	if garage.ID == id {
		json.NewEncoder(w).Encode(garage)
		return
	}
	json.NewEncoder(w).Encode(&Garage{})
}

func GetGarages(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	garages := getGarages()
	json.NewEncoder(w).Encode(garages)
}

func CreateGarage(w http.ResponseWriter, req *http.Request) {	
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	var garage Garage
	_ = json.NewDecoder(req.Body).Decode(&garage)
	createGarage(garage)
	json.NewEncoder(w).Encode(garages)
}

func DeleteGarage(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	id, _ := strconv.Atoi(params["id"])
	deleteGarage(id)
}

/*func UpdateGarage(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	params := mux.Vars(r)
	for index, item := range garages {
		if item.ID == params["id"] {
			garages = append(garages[:index], garages[index+1:]...)
			var garage Garage
			_ = json.NewDecoder(r.Body).Decode(&garage)
			garage.ID = params["id"]
			garages = append(garages, garage)
			json.NewEncoder(w).Encode(garage)
			return
		}
	}
}*/
