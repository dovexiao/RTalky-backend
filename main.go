package main

import (
	"RTalky/core/logger"
	"RTalky/http/routes"
	"RTalky/http/services"
	"os"

	_ "RTalky/database/ent/runtime"
	_ "github.com/go-sql-driver/mysql"
	"github.com/joho/godotenv"
	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
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
	bindAddress := os.Getenv("BIND_ADDRESS")

	// 启动HTTP服务器
	e := echo.New()

	routes.Register(e)
	services.Register()

	e.Logger.Fatal(e.Start(bindAddress))
}
