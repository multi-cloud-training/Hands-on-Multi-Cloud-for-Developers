package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"github.com/gorilla/mux"
)

type Car struct {
	ID     int `json:"ID"`
	UserID int `json:"UserID"`
	Model  string `json:"Model"`
}

const CARS_ROUTE string = "cars/"

func GetCar(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	car := Car{}
	id := params["id"]
	url := IP_ADDRESS + CARS_ROUTE + id
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}
	error := json.Unmarshal(contents, &car)
	if error != nil {
		panic(error)
	}
	json.NewEncoder(w).Encode(car)
}

func GetCars(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	cars := []Car{}
	url := IP_ADDRESS + CARS_ROUTE
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}
	error := json.Unmarshal(contents, &cars)
	if error != nil {
		panic(error)
	}

	json.NewEncoder(w).Encode(cars)
}

func GetCarsByUser(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	id := params["id"]

	cars := []Car{}
	url := IP_ADDRESS + "get-cars-by-user/" + id
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		fmt.Printf("%s", err)
		panic(err)
	}
	error := json.Unmarshal(contents, &cars)
	if error != nil {
		fmt.Printf("%s", error)
		panic(error)
	}
	json.NewEncoder(w).Encode(cars)
}

func CreateCar(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	var car Car
    _ = json.NewDecoder(req.Body).Decode(&car)

	url := IP_ADDRESS + CARS_ROUTE
	jsonStr, _ := json.Marshal(car)
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
	_ = json.NewDecoder(resp.Body).Decode(&car)
	json.NewEncoder(w).Encode(car)
}

func DeleteCar(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	id := params["id"]
	url := IP_ADDRESS + CARS_ROUTE + id
	response, err := http.NewRequest("DELETE", url, nil)
	defer response.Body.Close()

	if err != nil {
		panic(err)
	}
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
