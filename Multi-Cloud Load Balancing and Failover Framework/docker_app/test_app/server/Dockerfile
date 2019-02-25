FROM golang:1.10
RUN mkdir /app
ADD . /app/
WORKDIR /app
RUN go get -d github.com/gorilla/mux
RUN go get -d github.com/gorilla/securecookie
CMD ["go", "run", "Server.go", "Handlers.go"]
