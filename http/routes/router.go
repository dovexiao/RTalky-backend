package routes

import (
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/swaggo/echo-swagger"

	httpHandlers "RTalky/http/handlers"
	customMiddleware "RTalky/http/handlers/middleware"
	"RTalky/http/routes/feature"

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
// @BasePath /api
func Register(e *echo.Echo) {
	feature.SetupCORS(e)

	e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	e.GET("/swagger/*", echoSwagger.WrapHandler)

	api := e.Group("/api", customMiddleware.CustomContextMiddleware)

	// auth 模块
	authGroup := api.Group("/auth")
	authGroup.GET("/me", httpHandlers.Me)
	authGroup.GET("/captcha", httpHandlers.GenerateCaptcha)
	authGroup.GET("/captcha/email", httpHandlers.EmailCaptchaHandler)
	authGroup.POST("/login", httpHandlers.Login)
	authGroup.POST("/logout", httpHandlers.Logout)
	authGroup.POST("/signup", httpHandlers.SignUpHandler)

	// chat 模块
	chatGroup := api.Group("/chat")
	chatGroup.GET("/event", httpHandlers.ServerEventHandler)

	// profile模块
	profileGroup := api.Group("/profile", customMiddleware.AuthMiddleware)
	profileGroup.POST("/update", httpHandlers.EmptyHandler)
}
