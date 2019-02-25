package main

import (
	"log"
	"net/http"
	"os"
	"github.com/gorilla/mux"
)
var IP_ADDRESS string = os.Getenv("IP")
//Use "go run *.go" to run the program
// Main function
func main() {
	// Init router
	router := mux.NewRouter()

	// Hardcoded data - @todo: add database
/*	reservations = append(reservations, Reservation{ID: "1", StartTime: "438227", EndTime: "438227", CarID: "23", GarageID: "231"})
	reservations = append(reservations, Reservation{ID: "2", StartTime: "438227", EndTime: "438227", CarID: "11", GarageID: "232"})
	users = append(users, User{ID: "1", Username: "filik", Email: "yo@dot", Password: "1234"})
	users = append(users, User{ID: "1", Username: "filik", Email: "yo@dot", Password: "1234"})
	cars = append(cars, Car{ID: "1", UserID: "23", Model: "BMW"})
	cars = append(cars, Car{ID: "2", UserID: "23", Model: "MERCEDES"})
	garages = append(garages, Garage{ID: "1", Name: "1st", MaxCars: "5"})
	garages = append(garages, Garage{ID: "2", Name: "2nd", MaxCars: "3"})
	history = append(history, History{ID: "1", ReservationID: "2"})
*/
	// Reservation route handles & endpoints
	router.HandleFunc("/reservations", GetReservations).Methods("GET")
	router.HandleFunc("/reservations/", GetReservations).Methods("GET")
	router.HandleFunc("/reservations/{id}", GetReservation).Methods("GET")
	router.HandleFunc("/reservations/", CreateReservation).Methods("POST")
	router.HandleFunc("/reservations", CreateReservation).Methods("POST")
	//router.HandleFunc("/reservations/{id}", UpdateReservation).Methods("PUT")
	router.HandleFunc("/reservations/{id}", DeleteReservation).Methods("DELETE")
	router.HandleFunc("/reservations-by-user/{id}", GetReservationsByUser).Methods("GET")

	// Users route handles & endpoints
	router.HandleFunc("/users", GetUsers).Methods("GET")
	router.HandleFunc("/users/", GetUsers).Methods("GET")
	router.HandleFunc("/users/{id}", GetUser).Methods("GET")
	router.HandleFunc("/users", CreateUser).Methods("POST")
	router.HandleFunc("/users/", CreateUser).Methods("POST")
	//router.HandleFunc("/users/{id}", UpdateUser).Methods("PUT")
	router.HandleFunc("/users/{id}", DeleteUser).Methods("DELETE")

	// Garages route handles & endpoints
	router.HandleFunc("/garages", GetGarages).Methods("GET")
	router.HandleFunc("/garages/", GetGarages).Methods("GET")
	router.HandleFunc("/garages/{id}", GetGarage).Methods("GET")
	router.HandleFunc("/garages/", CreateGarage).Methods("POST")
	router.HandleFunc("/garages", CreateGarage).Methods("POST")
	//router.HandleFunc("/garages/{id}", UpdateGarage).Methods("PUT")
	router.HandleFunc("/garages/{id}", DeleteGarage).Methods("DELETE")

	// Cars route handles & endpoints
	router.HandleFunc("/cars", GetCars).Methods("GET")
	router.HandleFunc("/cars/", GetCars).Methods("GET")
	router.HandleFunc("/cars/{id}", GetCar).Methods("GET")
	router.HandleFunc("/cars", CreateCar).Methods("POST")
	router.HandleFunc("/cars", CreateCar).Methods("POST")
	router.HandleFunc("/get-cars-by-user/{id}", GetCarsByUser).Methods("GET")
	//router.HandleFunc("/cars/{id}", UpdateCar).Methods("PUT")
	router.HandleFunc("/cars/{id}", DeleteCar).Methods("DELETE")

	// History route handler & endpoint
	router.HandleFunc("/history/", GetHistory).Methods("GET")

	// Start server
	log.Fatal(http.ListenAndServe(":8888", router))
}
