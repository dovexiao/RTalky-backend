package routes

import (
	"RTalky/handlers"
	middleware2 "RTalky/handlers/middleware"
	"RTalky/routes/feature"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
)

func Register(e *echo.Echo) {
	feature.SetupCORS(e)

	e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	e.Use(middleware2.CustomContextMiddleware)

	api := e.Group("/api")

	// auth 模块
	authGroup := api.Group("/auth")
	authGroup.GET("/me", handlers.Me)
	authGroup.POST("/login", handlers.Login)
	authGroup.POST("/logout", handlers.Logout)

	// user 模块
	userGroup := api.Group("/user", middleware2.AuthMiddleware)
	userGroup.POST("/signup", handlers.EmptyHandler)
	userGroup.POST("/update/:uid", handlers.EmptyHandler)
}
