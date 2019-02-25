package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"io/ioutil"
	"github.com/gorilla/mux"
)

type User struct {
	ID       int `json:"ID"`
	Username string `json:"Username"`
	Email    string `json:"Email"`
	Password string `json:"Password"`
}

const USERS_ROUTE string = "users/"

func GetUser(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	user := User{}
	id := params["id"]
	url := IP_ADDRESS + USERS_ROUTE + id
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}
	error := json.Unmarshal(contents, &user)
	if error != nil {
		panic(error)
	}
	json.NewEncoder(w).Encode(user)
}

func GetUsers(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	users := []User{}
	url := IP_ADDRESS + USERS_ROUTE
	response, err := http.Get(url)

	defer response.Body.Close()
	contents, err := ioutil.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}
	error := json.Unmarshal(contents, &users)
	if error != nil {
		panic(error)
	}

	json.NewEncoder(w).Encode(users)
}

func CreateUser(w http.ResponseWriter, req *http.Request) {
	(w).Header().Set("Access-Control-Allow-Origin", "*")
	var user User
    _ = json.NewDecoder(req.Body).Decode(&user)

	url := IP_ADDRESS + USERS_ROUTE
	jsonStr, _ := json.Marshal(user)
	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonStr))
	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	_ = json.NewDecoder(resp.Body).Decode(&user)
	json.NewEncoder(w).Encode(user)
}

func DeleteUser(w http.ResponseWriter, req *http.Request) {
	params := mux.Vars(req)
	(w).Header().Set("Access-Control-Allow-Origin", "*")

	id := params["id"]
	url := IP_ADDRESS + USERS_ROUTE + id
	response, err := http.NewRequest("DELETE", url, nil)
	defer response.Body.Close()

	if err != nil {
		panic(err)
	}
}

/*func UpdateUser(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	params := mux.Vars(r)
	for index, item := range users {
		if item.ID == params["id"] {
			users = append(users[:index], users[index+1:]...)
			var user User
			_ = json.NewDecoder(r.Body).Decode(&user)
			user.ID = params["id"]
			users = append(users, user)
			json.NewEncoder(w).Encode(user)
			return
		}
	}
}*/
