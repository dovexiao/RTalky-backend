package main

import (
	"RTalky/core/logger"
	"RTalky/database"
	"RTalky/routes"
	"RTalky/services"
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
	dbURL := os.Getenv("DB_URL")
	dbDriver := os.Getenv("DB_DRIVER")
	bindAddress := os.Getenv("BIND_ADDRESS")

	// 启动数据库连接
	client, err := database.GetDataBaseClient(dbDriver, dbURL)
	if err != nil {
		logrus.Fatal("Fail to get database client: ", err.Error())
		return
	}

	// 启动HTTP服务器
	e := echo.New()

	routes.Register(e)
	services.Register(client)

	e.Logger.Fatal(e.Start(bindAddress))
}
