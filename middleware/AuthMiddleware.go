package middleware

import (
	"RTalky/utils"
	"github.com/labstack/echo/v4"
	"net/http"
	"os"
	"strings"
)

var jwtUtils *utils.JWTUtils

func AuthMiddleware(next echo.HandlerFunc) echo.HandlerFunc {
	if jwtUtils == nil {
		jwtUtils = utils.NewJWTUtils(os.Getenv("JWT_SECRET"), os.Getenv("JWT_EXPIRATION_TIME_MS"))
	}

	return func(c echo.Context) error {
		authHeader := c.Request().Header.Get("Authorization")
		if authHeader == "" {
			return c.JSON(http.StatusUnauthorized, map[string]string{
				"error": "Missing Authorization header",
			})
		}
		token := strings.TrimPrefix(authHeader, "Bearer ")

		claims, err := jwtUtils.ParseToken(token)

		if err != nil {
			return c.JSON(http.StatusUnauthorized, map[string]string{
				"error": "Missing Authorization header",
			})
		}

		// 将用户名注入上下文中
		c.Set("username", claims.Username)

		return next(c)
	}
}
