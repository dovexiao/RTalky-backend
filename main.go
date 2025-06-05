package main

import (
	"RTalky/logger"
	"RTalky/routes"
	"github.com/joho/godotenv"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
	"os"
)

func init() {
	err := godotenv.Load(".env")
	if err != nil {
		logrus.Fatal("Error loading .env file", err.Error())
	}

	// 初始化Logger
	logger.SetupLogger("./logs", logrus.DebugLevel)
}

func main() {
	e := echo.New()

	routes.Register(e)
	bindAddress := os.Getenv("BIND_ADDRESS")
	e.Logger.Fatal(e.Start(bindAddress))

}
