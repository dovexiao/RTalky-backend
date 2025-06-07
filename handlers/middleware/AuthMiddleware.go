package middleware

import (
	"net/http"
	"strings"

	"RTalky/handlers/responses"
	"RTalky/services"

	"github.com/labstack/echo/v4"
	"github.com/sirupsen/logrus"
)

func AuthMiddleware(next echo.HandlerFunc) echo.HandlerFunc {
	return func(c echo.Context) error {
		authHeader := c.Request().Header.Get("Authorization")
		if authHeader == "" {
			logrus.Trace("Token cannot be find in request header")
			responses.SetReturnValue(c, http.StatusUnauthorized, responses.UnauthorizedResponse)
			return nil
		}

		token := strings.TrimPrefix(authHeader, "Bearer ")
		claims, err := services.JwtUtils.ParseToken(token)

		if err != nil {
			logrus.Debug("Fail to auth the token: ", err)
			responses.SetReturnValue(c, http.StatusUnauthorized, responses.UnauthorizedResponse)
			return nil
		}

		// 将用户名注入上下文中
		c.Set("username", claims.Username)

		return next(c)
	}
}
