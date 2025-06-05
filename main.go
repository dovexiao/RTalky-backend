package main

import (
	"RTalky/logger"
	"RTalky/routes"
	"github.com/joho/godotenv"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
	"os"
)

func main() {
	err := godotenv.Load()
	if err != nil {
		logrus.Fatal("Error loading .env file")
	}

	// 初始化Logger
	logger.SetupLogger("./logs", logrus.DebugLevel)

	e := echo.New()

	routes.Register(e)
	bindAddress := os.Getenv("BIND_ADDRESS")
	e.Logger.Fatal(e.Start(bindAddress))

}
