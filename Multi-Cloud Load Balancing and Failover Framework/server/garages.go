package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"io/ioutil"
	"github.com/gorilla/mux"
)

type Garage struct {
	ID      int `json:"ID"`
	Name    string `json:"Name"`
	MaxCars int `json:"MaxCars"`
}

const GARAGES_ROUTE string = "garages/"

func GetGarage(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	
	garage := Garage{}
	id := params["id"]

	url := IP_ADDRESS + GARAGES_ROUTE + id
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}
	error := json.Unmarshal(contents, &garage)
	if error != nil {
		panic(error)
	}
	json.NewEncoder(w).Encode(garage)
}

func GetGarages(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	garages := []Garage{}
	url := IP_ADDRESS + GARAGES_ROUTE
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}
	error := json.Unmarshal(contents, &garages)
	if error != nil {
		panic(err)
	}

	json.NewEncoder(w).Encode(garages)
}

func CreateGarage(w http.ResponseWriter, req *http.Request) {	
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	var garage Garage
    _ = json.NewDecoder(req.Body).Decode(&garage)

	url := IP_ADDRESS + GARAGES_ROUTE
	jsonStr, _ := json.Marshal(garage)
	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonStr))
	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	_ = json.NewDecoder(resp.Body).Decode(&garage)
	json.NewEncoder(w).Encode(garage)
}

func DeleteGarage(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	id := params["id"]
	url := IP_ADDRESS + GARAGES_ROUTE + id
	response, err := http.NewRequest("DELETE", url, nil)
	defer response.Body.Close()

	if err != nil {
		panic(err)
	}
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
