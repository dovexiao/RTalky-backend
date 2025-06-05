package main

import (
	"context"
	"os"

	"RTalky/ent"
	_ "RTalky/ent/runtime"
	"RTalky/logger"
	"RTalky/routes"

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

	client, err := ent.Open(dbDriver, dbURL)
	if err != nil {
		logrus.Fatal("连接数据库失败: ", err.Error())
	}
	defer client.Close()

	// Run the auto migration tool.
	ctx := context.Background()
	if err := client.Schema.Create(ctx); err != nil {
		logrus.Fatal("连接数据库失败")
	}
	_, err = client.User.Create().SetUsername("test@test.com").SetPassword("test123").SetNickname("test").SetIntroduction("test").Save(ctx)
	if err != nil {
		logrus.Fatalf("%v", err)
		return
	}

	// 启动HTTP服务器
	e := echo.New()

	routes.Register(e)
	bindAddress := os.Getenv("BIND_ADDRESS")
	e.Logger.Fatal(e.Start(bindAddress))

}
