//go:build !dev

package feature

import (
	"github.com/labstack/echo/v4"
)

func SetupCORS(_ *echo.Echo) {
}
