//go:build dev

package feature

import (
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/sirupsen/logrus"
)

func SetupCORS(e *echo.Echo) {
	e.Use(middleware.CORS())
	logrus.Warn("CORS 已开启 — 生产环境请关闭！开发环境请忽略此条消息")
}
