package middleware

import (
	"RTalky/core"
	"github.com/labstack/echo/v4"
)

func CustomContextMiddleware(next echo.HandlerFunc) echo.HandlerFunc {
	return func(c echo.Context) error {
		cc := &core.CustomContext{Context: c}
		return next(cc)
	}
}
