package routes

import (
	"RTalky/handlers"
	mymiddleware "RTalky/handlers/middleware"
	"RTalky/routes/feature"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/swaggo/echo-swagger"

	_ "RTalky/docs"
)

// Register Swagger API Config
// @title RTalky Swagger API
// @version 1.0
// @description This is a RTalky server.
// @termsOfService http://swagger.io/terms/
// @contact.name API Support
// @contact.url http://www.swagger.io/support
// @contact.email support@swagger.io
// @license.name Apache 2.0
// @license.url http://www.apache.org/licenses/LICENSE-2.0.html
// @host localhost:8080
// @BasePath /api
func Register(e *echo.Echo) {
	feature.SetupCORS(e)

	e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	e.GET("/swagger/*", echoSwagger.WrapHandler)

	api := e.Group("/api", mymiddleware.CustomContextMiddleware)

	// auth 模块
	authGroup := api.Group("/auth")
	authGroup.GET("/me", handlers.Me)
	authGroup.POST("/login", handlers.Login)
	authGroup.POST("/logout", handlers.Logout)

	// user 模块
	userGroup := api.Group("/user", mymiddleware.AuthMiddleware)
	userGroup.POST("/signup", handlers.EmptyHandler)
	userGroup.POST("/update/:uid", handlers.EmptyHandler)
}
