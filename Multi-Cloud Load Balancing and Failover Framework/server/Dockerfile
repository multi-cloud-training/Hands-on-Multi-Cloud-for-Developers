FROM golang
RUN go get -u github.com/gorilla/mux
RUN go get github.com/gorilla/securecookie
COPY . .
EXPOSE 8888
CMD go run *.go
