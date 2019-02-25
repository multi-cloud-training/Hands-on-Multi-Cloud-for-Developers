package main

import (
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"os"
	"time"

	"github.com/nicholasjackson/bench"
	"github.com/nicholasjackson/bench/output"
	"github.com/nicholasjackson/bench/util"
)

var client *http.Client

func main() {
	fmt.Println("Benchmarking application")

	client = &http.Client{
		Transport: &http.Transport{
			MaxIdleConnsPerHost: 100,
		},
		Timeout: 5 * time.Second,
	}

	b := bench.New(100, 300*time.Second, 90*time.Second, 5*time.Second)
	b.AddOutput(301*time.Second, os.Stdout, output.WriteTabularData)
	b.AddOutput(1*time.Second, util.NewFile("./output.txt"), output.WriteTabularData)
	b.AddOutput(1*time.Second, util.NewFile("./error.txt"), output.WriteErrorLogs)
	b.AddOutput(1*time.Second, util.NewFile("./output.png"), output.PlotData)
	b.RunBenchmarks(NomadRequest)
}

func NomadRequest() error {

	data := []byte(`{"name": "nic", "sku":"22323"}`)

	req, err := http.NewRequest("POST", "http://nomad.demo.gs/product", bytes.NewBuffer(data))

	resp, err := client.Do(req)
	if err != nil {
		return err
	}

	io.Copy(ioutil.Discard, resp.Body)
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("Failed with status: %v", resp.Status)
	}

	return nil
}
