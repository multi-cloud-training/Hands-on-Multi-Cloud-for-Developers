package main

import (
	"encoding/json"
	"net/http"
)

type History struct {
	ID            int `json:"ID"`
	ReservationID int `json:"ReservationID"`
}

var history []History

func GetHistory(w http.ResponseWriter, req *http.Request) {
	json.NewEncoder(w).Encode(history)
}
