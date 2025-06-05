package routes

import (
	"RTalky/handlers"
	mymiddleware "RTalky/middleware"
	"RTalky/routes/feature"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
)

func Register(e *echo.Echo) {
	feature.SetupCORS(e)

	e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	api := e.Group("/api")

	// auth 模块
	authGroup := api.Group("/auth")
	authGroup.GET("/me", handlers.Me)
	authGroup.POST("/login", handlers.EmptyHandler)
	authGroup.POST("/logout", handlers.EmptyHandler)

	// user 模块
	userGroup := api.Group("/user", mymiddleware.AuthMiddleware)
	userGroup.POST("/signup", handlers.EmptyHandler)
	userGroup.POST("/update/:uid", handlers.EmptyHandler)
}
